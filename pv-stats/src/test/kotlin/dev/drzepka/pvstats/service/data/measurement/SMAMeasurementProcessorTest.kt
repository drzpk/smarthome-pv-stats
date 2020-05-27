package dev.drzepka.pvstats.service.data.measurement

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import dev.drzepka.pvstats.common.model.sma.Entry
import dev.drzepka.pvstats.common.model.sma.SMADeviceData
import dev.drzepka.pvstats.common.model.sma.SMAMeasurement
import dev.drzepka.pvstats.common.model.vendor.SMAData
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.service.DeviceDataService
import dev.drzepka.pvstats.service.data.MeasurementService
import dev.drzepka.pvstats.util.kAny
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class SMAMeasurementProcessorTest {

    @Suppress("UNCHECKED_CAST")
    private val measurementService = mock<MeasurementService> {
        on { getLastMeasurement(kAny()) } doAnswer { storedMeasurements.lastOrNull() }
        on { storeNewMeasurements(kAny(), kAny()) } doAnswer {
            storedMeasurements.addAll(it.arguments[0] as List<EnergyMeasurement>)
        }
    }
    private val deviceDataService = mock<DeviceDataService> {}

    private var storedMeasurements = ArrayList<EnergyMeasurement>()

    @Test
    fun `check if new measurements are correct`() {
        val now = Instant.now()
        val entry1 = Entry(Date.from(now.minusSeconds(60)), 100)
        val entry2 = Entry(Date.from(now.minusSeconds(30)), 200)
        val entries = listOf(entry1, entry2)

        val measurement = EnergyMeasurement()
        measurement.timestamp = Date.from(Instant.now().minus(1, ChronoUnit.DAYS))
        storedMeasurements.add(measurement)

        val processor = SMAMeasurementProcessor(measurementService, deviceDataService)
        processor.process(getDevice(), getSMAData(entries))

        then(storedMeasurements).hasSize(3) // one pre-existing plus two new
        val firstNew = storedMeasurements[1]
        then(firstNew.totalWh).isEqualTo(100)
        then(firstNew.deltaWh).isEqualTo(100)
        val lastNew = storedMeasurements.last()
        then(lastNew.totalWh).isEqualTo(200)
        then(lastNew.deltaWh).isEqualTo(100)
        then(lastNew.powerW).isEqualTo(12_000)
    }

    @Test
    fun `check if measurements don't overlap - common element`() {
        // There's a common element (with matching timestamp) between new and stored measurements
        val now = Instant.now()

        val existing1 = EnergyMeasurement()
        existing1.timestamp = Date.from(now.minusSeconds(120))
        storedMeasurements.add(existing1)
        val existing2 = EnergyMeasurement()
        existing2.timestamp = Date.from(now.minusSeconds(60))
        storedMeasurements.add(existing2)

        val entry1 = Entry(existing2.timestamp, 100) // this one overlaps
        val entry2 = Entry(Date.from(now), 200)
        val entries = listOf(entry1, entry2)

        val processor = SMAMeasurementProcessor(measurementService, deviceDataService)
        processor.process(getDevice(), getSMAData(entries))

        then(storedMeasurements).hasSize(3)
        then(storedMeasurements[0].timestamp).isEqualTo(existing1.timestamp)
        then(storedMeasurements[1].timestamp).isEqualTo(existing2.timestamp)
        then(storedMeasurements[2].timestamp).isEqualTo(entry2.t)
    }

    @Test
    fun `check if measurements don't overlap - no common element`() {
        // There isn't a common element (no matching timestampe)
        val now = Instant.now()

        val existing1 = EnergyMeasurement()
        existing1.timestamp = Date.from(now.minusSeconds(120))
        storedMeasurements.add(existing1)
        val existing2 = EnergyMeasurement()
        existing2.timestamp = Date.from(now.minusSeconds(60))
        storedMeasurements.add(existing2)

        val entry1 = Entry(Date.from(now.minusSeconds(70)), 100) // this one overlaps
        val entry2 = Entry(Date.from(now), 200)
        val entries = listOf(entry1, entry2)

        val processor = SMAMeasurementProcessor(measurementService, deviceDataService)
        processor.process(getDevice(), getSMAData(entries))

        then(storedMeasurements).hasSize(3)
        then(storedMeasurements[0].timestamp).isEqualTo(existing1.timestamp)
        then(storedMeasurements[1].timestamp).isEqualTo(existing2.timestamp)
        then(storedMeasurements[2].timestamp).isEqualTo(entry2.t)
    }

    private fun getSMAData(entries: List<Entry>): SMAData {
        val subsection = hashMapOf(Pair("1", entries))
        val deviceData = SMADeviceData()
        deviceData.currentData = subsection
        val measurement = SMAMeasurement()
        measurement.result["device-id"] = deviceData

        return SMAData(measurement, null)
    }

    private fun getDevice(): Device {
        val device = Device()
        device.name = "test device"
        device.apiUrl = "http://localhost"
        return device
    }

}