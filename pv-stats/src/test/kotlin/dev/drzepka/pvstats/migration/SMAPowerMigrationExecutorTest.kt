package dev.drzepka.pvstats.migration

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.model.DeviceType
import dev.drzepka.pvstats.repository.DeviceRepository
import dev.drzepka.pvstats.repository.MeasurementRepository
import dev.drzepka.pvstats.util.kAny
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

class SMAPowerMigrationExecutorTest {

    private val deviceRepository = mock<DeviceRepository> {
        on { findAll() } doReturn getDevices()
    }

    private val measurementRepository = mock<MeasurementRepository> {
        on { findFirstByDeviceIdOrderByTimestampAsc(Mockito.anyInt()) } doAnswer {
            measurements?.get(0)
        }
        on { findForDateRange(Mockito.anyInt(), kAny(), kAny()) } doAnswer {
            from = it.arguments[1] as Date
            to = it.arguments[2] as Date

            if (!getAllCalled) {
                getAllCalled = true
                measurements
            } else {
                emptyList()
            }
        }
    }

    private var measurements: List<EnergyMeasurement>? = null
    private var getAllCalled = false
    private var from: Date? = null
    private var to: Date? = null

    @Test
    fun `check date range calculation`() {
        measurements = listOf(getMeasurement(1, 0, 0, 0, 0))

        getExecutor().execute()

        val startTime = LocalDate.now().atStartOfDay()
        Assertions.assertEquals(Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()), from)
        Assertions.assertEquals(Date.from(startTime.with(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()), to)
    }

    @Test
    fun `check power calculation`() {
        measurements = listOf(
                getMeasurement(1, 100, 10, 0, 0),
                getMeasurement(2, 200, 10, 5, 0),
                getMeasurement(3, 300, 10, 10, 10)
        )

        getExecutor().execute()

        Assertions.assertEquals(0, measurements!![0].powerW)
        Assertions.assertEquals(2400, measurements!![1].powerW)
        Assertions.assertEquals(3484, measurements!![2].powerW)
    }

    private fun getExecutor(): SMAPowerMigrationExecutor = SMAPowerMigrationExecutor(deviceRepository, measurementRepository)

    private fun getDevices(): List<Device> {
        val smaDevice = Device()
        smaDevice.id = 1
        smaDevice.name = "sma"
        smaDevice.type = DeviceType.SMA

        val sofarDevice = Device()
        sofarDevice.id = 2
        sofarDevice.name = "sofar"
        sofarDevice.type = DeviceType.SOFAR

        return listOf(smaDevice, sofarDevice)
    }

    private fun getMeasurement(id: Int, deltaWh: Int, hour: Int, minute: Int, second: Int, date: LocalDate = LocalDate.now()): EnergyMeasurement {
        val measurement = EnergyMeasurement()
        measurement.id = id
        measurement.deltaWh = deltaWh
        measurement.timestamp = Date.from(date
                .atStartOfDay()
                .with(LocalTime.of(hour, minute, second))
                .atZone(ZoneId.systemDefault())
                .toInstant())
        return measurement
    }
}