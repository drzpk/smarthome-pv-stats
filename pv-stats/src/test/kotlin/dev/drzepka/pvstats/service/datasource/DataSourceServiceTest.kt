package dev.drzepka.pvstats.service.datasource

import com.nhaarman.mockitokotlin2.*
import dev.drzepka.pvstats.entity.DataSource
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.model.ApplicationException
import dev.drzepka.pvstats.repository.DataSourceRepository
import dev.drzepka.pvstats.repository.SchemaManagementRepository
import dev.drzepka.pvstats.service.DeviceService
import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import java.util.*

class DataSourceServiceTest {

    @Test
    fun `should throw exception if device doesn't exist`() {
        Assertions.assertThrows(ApplicationException::class.java) {
            getService().createDataSource(999)
        }
    }

    @Test
    fun `should throw exception if data source already exists`() {
        val deviceService = mock<DeviceService> {
            on { getDevice(any()) } doReturn getDevice(1)
        }
        val dataSourceRepository = mock<DataSourceRepository> {
            on { findByDevice(any()) } doReturn getDataSource(1)
        }

        Assertions.assertThrows(ApplicationException::class.java) {
            getService(deviceService = deviceService, dataSourceRepository = dataSourceRepository).createDataSource(1)
        }
    }

    @Test
    fun `should create data source`() {
        val sourceSchema = "schema"

        val deviceService = mock<DeviceService> {
            on { getDevice(any()) } doReturn getDevice(2, "device")
        }
        val dataSourceRepository = mock<DataSourceRepository> {
            on { save<DataSource>(any()) } doAnswer { it.arguments[0] as DataSource }
        }
        val schemaManagementRepository = mock<SchemaManagementRepository> {
            on { getMainSchemaName() } doReturn sourceSchema
            on { getMainSchemaVersion() } doReturn 1
        }

        val credentials = getService(deviceService, dataSourceRepository, schemaManagementRepository)
                .createDataSource(2)
        val dataSource = credentials.dataSource

        verify(schemaManagementRepository, times(1)).createUser(argThat { this == dataSource.user }, argThat { this == credentials.plainPassword })
        verify(schemaManagementRepository, times(1)).createSchema(argThat { this == dataSource.schema })
        verify(schemaManagementRepository, times(1)).revokeAllPrivileges(argThat { this == dataSource.user })
        verify(schemaManagementRepository, times(1)).grantSelectPrivilegesToSchema(argThat { this == dataSource.user }, argThat { this == dataSource.schema })

        verify(schemaManagementRepository, times(1)).createView(
                argThat { this == dataSource.schema },
                eq("energy_measurement"),
                eq(sourceSchema),
                eq("energy_measurement"),
                any()
        )
        verify(schemaManagementRepository, times(1)).createView(
                argThat { this == dataSource.schema },
                eq("energy_measurement_daily_summary"),
                eq(sourceSchema),
                eq("energy_measurement_daily_summary"),
                any()
        )
    }

    @Test
    fun `should delete data source`() {
        val dataSourceId = 1
        val dataSource = getDataSource(dataSourceId)
        dataSource.schema = "schema"
        dataSource.user = "user"

        val dataSourceRepository = mock<DataSourceRepository> {
            on { findById(eq(dataSourceId)) } doReturn Optional.of(dataSource)
        }
        val schemaManagementRepository = mock<SchemaManagementRepository> {}

        val service = getService(
                dataSourceRepository = dataSourceRepository,
                schemaManagementRepository = schemaManagementRepository
        )

        service.deleteDataSource(dataSourceId)

        verify(schemaManagementRepository, times(1)).dropSchema(eq(dataSource.schema))
        verify(schemaManagementRepository, times(1)).dropUser(eq(dataSource.user))
    }

    @Test
    fun `should reset password`() {
        val dataSourceId = 2
        val dataSource = getDataSource(dataSourceId)
        dataSource.user = "user"
        dataSource.password = "old"

        val dataSourceRepository = mock<DataSourceRepository> {
            on { findById(eq(dataSourceId)) } doReturn Optional.of(dataSource)
            on { save<DataSource>(any()) } doAnswer { it.arguments[0] as DataSource }
        }
        val schemaManagementRepository = mock<SchemaManagementRepository> {}

        val service = getService(
                dataSourceRepository = dataSourceRepository,
                schemaManagementRepository = schemaManagementRepository
        )

        service.resetPassword(dataSourceId)

        verify(schemaManagementRepository, times(1)).changeUserPassword(
                eq(dataSource.user), argThat { this != dataSource.password })
    }

    @Test
    fun `should throw exception if table in main schema doesn't exist`() {
        val schemaManagementRepository = mock<SchemaManagementRepository> {
            on { getMainSchemaVersion() } doReturn 1
            on { tableExists(any(), any() )} doReturn false
        }

        Assertions.assertThrows(IllegalStateException::class.java) {
            getService(schemaManagementRepository = schemaManagementRepository).checkAvailableViewNames()
        }
    }

    @Test
    fun `should update out-of-date data sources`() {
        val currentSchemaVersion = 5
        val updatedDataSource = getDataSource(1)
        updatedDataSource.updatedAtVersion = currentSchemaVersion
        val oldDataSource = getDataSource(2)
        oldDataSource.updatedAtVersion = currentSchemaVersion - 1
        oldDataSource.device = getDevice(2, "test")

        val schemaManagementRepository = mock<SchemaManagementRepository> {
            on { getMainSchemaVersion() } doReturn currentSchemaVersion
        }
        val dataSourceRepository = mock<DataSourceRepository> {
            on { findAll() } doReturn listOf(updatedDataSource, oldDataSource)
        }

        val service = getService(schemaManagementRepository = schemaManagementRepository,
                dataSourceRepository = dataSourceRepository)
        service.checkDataSources()

        BDDAssertions.then(oldDataSource.updatedAtVersion).isEqualTo(currentSchemaVersion)
        verify(dataSourceRepository, times(1)).save(eq(oldDataSource))
        verify(dataSourceRepository, times(0)).save(eq(updatedDataSource))
    }

    private fun getService(
            deviceService: DeviceService = mock(),
            dataSourceRepository: DataSourceRepository = mock(),
            schemaManagementRepository: SchemaManagementRepository = mock()
    ): DataSourceService {
        return DataSourceService(deviceService, dataSourceRepository, schemaManagementRepository, PasswordEncoderFactories.createDelegatingPasswordEncoder(), mock())
    }

    private fun getDevice(id: Int, name: String = ""): Device = Device().apply {
        this.id = id
        this.name = name
    }

    private fun getDataSource(id: Int): DataSource = DataSource().apply { this.id = id }
}