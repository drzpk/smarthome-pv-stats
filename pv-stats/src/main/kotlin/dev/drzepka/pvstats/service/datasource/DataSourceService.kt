package dev.drzepka.pvstats.service.datasource

import dev.drzepka.pvstats.entity.DataSource
import dev.drzepka.pvstats.model.ApplicationException
import dev.drzepka.pvstats.model.datasource.DataSourceCredentials
import dev.drzepka.pvstats.repository.DataSourceRepository
import dev.drzepka.pvstats.repository.SchemaManagementRepository
import dev.drzepka.pvstats.service.DeviceService
import dev.drzepka.pvstats.util.DataSourceUtils
import dev.drzepka.pvstats.util.Logger
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.security.SecureRandom
import javax.annotation.PostConstruct
import javax.transaction.Transactional

@Service
class DataSourceService(
        private val deviceService: DeviceService,
        private val dataSourceRepository: DataSourceRepository,
        private val schemaManagementRepository: SchemaManagementRepository,
        private val passwordEncoder: PasswordEncoder,
        transactionManager: PlatformTransactionManager
) {

    private val log by Logger()
    private val transactionTemplate = TransactionTemplate(transactionManager)
    private val secureRandom = SecureRandom()

    @PostConstruct
    fun init() {
        checkAvailableViewNames()
        transactionTemplate.execute {
            checkDataSources()
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun createDataSource(deviceId: Int): DataSourceCredentials {
        log.info("Creating new data source for device $deviceId")
        val device = deviceService.getDevice(deviceId)
                ?: throw ApplicationException("device with id $deviceId wasn't found")

        val existing = dataSourceRepository.findByDevice(device)
        if (existing != null)
            throw ApplicationException("data source ${existing.id} for given device ${device.id} already exists")

        var dataSource = DataSource()
        val plainPassword = generateUserPassword()
        dataSource.schema = DataSourceUtils.generateSchemaName(device.name)
        dataSource.user = DataSourceUtils.generateUserName(device.name)
        dataSource.password = passwordEncoder.encode(plainPassword)
        dataSource.device = device
        dataSource.updatedAtVersion = schemaManagementRepository.getMainSchemaVersion()

        dataSource = dataSourceRepository.save(dataSource)
        log.info("Created data source $dataSource")

        val credentials = DataSourceCredentials(dataSource, plainPassword)
        createUser(credentials)
        createSchema(dataSource.schema)
        recreateViewsAndPrivileges(dataSource)

        return credentials
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun deleteDataSource(dataSourceId: Int) {
        val dataSource = dataSourceRepository.findById(dataSourceId).orElse(null)
                ?: throw ApplicationException("data source with id $dataSourceId wasn't found")

        deleteSchema(dataSource.schema)
        deleteUser(dataSource.user)
        dataSourceRepository.delete(dataSource)
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun resetPassword(dataSourceId: Int): DataSourceCredentials {
        log.info("Resetting password for data source $dataSourceId")
        var dataSource = dataSourceRepository.findById(dataSourceId).orElse(null)
                ?: throw ApplicationException("data source with id $dataSourceId wasn't found")

        val plainPassword = generateUserPassword()
        schemaManagementRepository.changeUserPassword(dataSource.user, plainPassword)
        dataSource.password = passwordEncoder.encode(plainPassword)
        dataSource = dataSourceRepository.save(dataSource)

        return DataSourceCredentials(dataSource, plainPassword)
    }

    fun getDataSources(): List<DataSource> = dataSourceRepository.findAll().toList()

    internal fun checkAvailableViewNames() {
        log.info("Validating view names")
        val mainSchemaName = schemaManagementRepository.getMainSchemaName()
        AVAILABLE_VIEWS.forEach {
            if (!schemaManagementRepository.tableExists(mainSchemaName, it))
                throw IllegalStateException("Table $mainSchemaName.$it doesn't exist")
        }
    }

    internal fun checkDataSources() {
        log.info("Validating data sources")
        val currentSchemaVersion = schemaManagementRepository.getMainSchemaVersion()
        dataSourceRepository.findAll()
                .filter { it.updatedAtVersion != currentSchemaVersion }
                .forEach {
                    log.info("Data source ${it.id} (${it.device!!.name}) was created for older schema, " +
                            "migrating: ${it.updatedAtVersion} -> $currentSchemaVersion")

                    recreateViewsAndPrivileges(it)
                    it.updatedAtVersion = currentSchemaVersion
                    dataSourceRepository.save(it)
                }
    }

    private fun createUser(credentials: DataSourceCredentials) {
        log.info("Creating user `${credentials.dataSource.user}`")
        schemaManagementRepository.createUser(credentials.dataSource.user, credentials.plainPassword)
    }

    private fun createSchema(schemaName: String) {
        log.info("Creating scheam $schemaName")
        schemaManagementRepository.createSchema(schemaName)
    }

    private fun recreateViewsAndPrivileges(dataSource: DataSource) {
        log.info("Recreating views for datasource ${dataSource.id} (${dataSource.device?.name})")
        deleteViews(dataSource)

        log.debug("Revoking all privileges from user ${dataSource.user}")
        schemaManagementRepository.revokeAllPrivileges(dataSource.user)

        val mainSchemaName = schemaManagementRepository.getMainSchemaName()
        AVAILABLE_VIEWS.forEach { viewName ->
            log.debug("Creating view ${dataSource.schema}.$viewName")
            schemaManagementRepository.createView(dataSource.schema, viewName, mainSchemaName, viewName, dataSource.device!!.id)
        }

        log.debug("Granting SELECT privileges on schema ${dataSource.schema} to user ${dataSource.user}")
        schemaManagementRepository.grantSelectPrivilegesToSchema(dataSource.user, dataSource.schema)
    }

    private fun deleteSchema(schemaName: String) {
        log.info("Deleting schema $schemaName")
        schemaManagementRepository.dropSchema(schemaName)
    }

    private fun deleteUser(userName: String) {
        log.info("Deleting user $userName")
        schemaManagementRepository.dropUser(userName)
    }

    private fun deleteViews(dataSource: DataSource) {
        log.info("Deleting views for data source ${dataSource.id} (device: ${dataSource.device?.name}, schema: ${dataSource.schema})")
        schemaManagementRepository.getViewNames(dataSource.schema).forEach { viewName ->
            if (!AVAILABLE_VIEWS.contains(viewName))
                log.warn("Schema ${dataSource.schema} contains table $viewName that isn't registerd")

            log.debug("Deleting view $viewName")
            schemaManagementRepository.dropView(dataSource.schema, viewName)
        }
    }

    private fun generateUserPassword(): String =
            (0 until PASSWORD_LENGTH)
                    .map { AVAILABLE_PASSWORD_CHARS[secureRandom.nextInt(AVAILABLE_PASSWORD_CHARS.length)] }
                    .joinToString(separator = "")

    companion object {
        internal val AVAILABLE_VIEWS = listOf("energy_measurement", "energy_measurement_daily_summary")


        private const val AVAILABLE_PASSWORD_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        private const val PASSWORD_LENGTH = 20
    }
}