package dev.drzepka.pvstats.service.datasource

import dev.drzepka.pvstats.entity.DataSource
import dev.drzepka.pvstats.model.ApplicationException
import dev.drzepka.pvstats.model.datasource.DataSourceCredentials
import dev.drzepka.pvstats.repository.DataSourceRepository
import dev.drzepka.pvstats.repository.SchemaManagementRepository
import dev.drzepka.pvstats.service.DeviceService
import dev.drzepka.pvstats.util.Logger
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import javax.annotation.PostConstruct
import javax.transaction.Transactional
import kotlin.random.Random

@Service
class DataSourceService(
        private val deviceService: DeviceService,
        private val dataSourceRepository: DataSourceRepository,
        private val schemaManagementRepository: SchemaManagementRepository,
        private val passwordEncoder: PasswordEncoder
) {

    private val log by Logger()
    private val secureRandom = SecureRandom()

    @PostConstruct
    fun init() {
        checkAvailableViewNames()
        checkDataSources()
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
        dataSource.user = generateUserName()
        dataSource.password = passwordEncoder.encode(plainPassword)
        dataSource.device = device
        dataSource.updatedAtVersion = schemaManagementRepository.getSchemaVersion()

        dataSource = dataSourceRepository.save(dataSource)
        log.info("Created data source $dataSource")

        val credentials = DataSourceCredentials(dataSource, plainPassword)
        createUser(credentials)
        recreateViews(dataSource)

        return credentials
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun deleteDataSource(dataSourceId: Int) {
        val dataSource = dataSourceRepository.findById(dataSourceId).orElse(null)
                ?: throw ApplicationException("data source with id $dataSourceId wasn't found")

        deleteViews(dataSource)
        deleteUser(dataSource.user)
        dataSourceRepository.delete(dataSource)
    }

    fun resetPassword(dataSourceId: Int): DataSourceCredentials {
        log.info("Resetting password for data source $dataSourceId")
        var dataSource = dataSourceRepository.findById(dataSourceId).orElse(null)
                ?: throw ApplicationException("data source with id $dataSourceId wasn't found")

        val plainPassword = generateUserPassword()
        dataSource.password = passwordEncoder.encode(plainPassword)
        dataSource = dataSourceRepository.save(dataSource)

        return DataSourceCredentials(dataSource, plainPassword)
    }

    fun getDataSources(): List<DataSource> = dataSourceRepository.findAll().toList()

    internal fun checkAvailableViewNames() {
        log.info("Validating view names")
        AVAILABLE_VIEWS.forEach {
            if (!schemaManagementRepository.tableExists(it))
                throw IllegalStateException("Table $it doesn't exist")
        }
    }

    internal fun checkDataSources() {
        log.info("Validating data sources")
        val currentSchemaVersion = schemaManagementRepository.getSchemaVersion()
        dataSourceRepository.findAll()
                .filter { it.updatedAtVersion != currentSchemaVersion }
                .forEach {
                    log.info("Data source ${it.id} (${it.device!!.name}) was created for older schema, " +
                            "migrating: ${it.updatedAtVersion} -> $currentSchemaVersion")

                    recreateViews(it)
                    it.updatedAtVersion = currentSchemaVersion
                    dataSourceRepository.save(it)
                }
    }

    private fun createUser(credentials: DataSourceCredentials) {
        log.info("Creating user `${credentials.dataSource.user}`")
        schemaManagementRepository.createUser(credentials.dataSource.user, credentials.password)
    }

    private fun deleteUser(userName: String) {
        log.info("Deleting user $userName")
        schemaManagementRepository.dropUser(userName)
    }

    private fun recreateViews(dataSource: DataSource) {
        log.info("Recreating views for datasource ${dataSource.id}")
        deleteViews(dataSource)

        val schemaName = schemaManagementRepository.getSchemaName()
        AVAILABLE_VIEWS.forEach {
            val viewName = VIEW_PREFIX + dataSource.id + "_" + it
            log.debug("Creating view $viewName")
            schemaManagementRepository.createView(viewName, it, dataSource.device!!.id)

            log.debug("Granting user ${dataSource.user} privilege to view $viewName")
            schemaManagementRepository.grantSelectPrivilegesToTable(schemaName, viewName, dataSource.user)
        }
    }

    private fun deleteViews(dataSource: DataSource) {
        log.info("Deleting views for data source ${dataSource.id}")
        schemaManagementRepository.getViewNames().forEach {
            val match = VIEW_DEVICE_ID_EXTRACTOR.matchEntire(it)
            if (match == null) {
                log.warn("Found view '$it' that doesn't match view naming pattern")
                return@forEach
            }

            val dataSourceId = match.groupValues[1].toInt()
            if (dataSourceId != dataSource.id) return@forEach

            log.debug("Deleting view $it")
            schemaManagementRepository.dropView(it)
        }
    }

    private fun generateUserName() = USER_PREFIX + Random.Default.nextInt(1000).toString().padStart(length = 3, padChar = '0')

    private fun generateUserPassword(): String =
            (0 until PASSWORD_LENGTH)
                    .map { AVAILABLE_PASSWORD_CHARS[secureRandom.nextInt(AVAILABLE_PASSWORD_CHARS.length)] }
                    .joinToString(separator = "")

    companion object {
        internal val AVAILABLE_VIEWS = listOf("energy_measurement")

        private const val VIEW_PREFIX = "view_"
        private const val USER_PREFIX = "viewer_"
        private val VIEW_DEVICE_ID_EXTRACTOR = Regex("^view_(\\d+)_.*\$")

        private const val AVAILABLE_PASSWORD_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        private const val PASSWORD_LENGTH = 20
    }
}