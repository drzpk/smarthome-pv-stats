package dev.drzepka.pvstats.service.connector

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.model.device.sma.Entry
import dev.drzepka.pvstats.model.device.sma.SMADeviceData
import dev.drzepka.pvstats.model.device.sma.SMAMeasurement
import dev.drzepka.pvstats.web.client.sma.SMAFeignClient
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class SMAConnectorTest {

    private var entries = emptyList<Entry>()

    @Test
    fun `verify data format`() {
        val realData = loadData()

        val connector = SMAConnector(getApiClient(realData))
        val measurements = connector.collectMeasurements(getDevice(), getFirstMeasurement())

        then(measurements.size).isGreaterThan(0)
    }

    @Test
    fun `check if new measurements are correct`() {
        val entry1 = Entry(Date.from(Instant.now().minusSeconds(60)), 100)
        val entry2 = Entry(Date.from(Instant.now().minusSeconds(30)), 200)
        entries = listOf(entry1, entry2)

        val connector = SMAConnector(getApiClient())
        val measurements = connector.collectMeasurements(getDevice(), getFirstMeasurement())

        then(measurements).hasSize(2)
        val first = measurements.first()
        then(first.totalWh).isEqualTo(100)
        then(first.deltaWh).isEqualTo(100)
        val last = measurements.last()
        then(last.totalWh).isEqualTo(200)
        then(last.deltaWh).isEqualTo(100)
    }

    private fun getApiClient(source: SMAMeasurement = createData()): SMAFeignClient = object : SMAFeignClient {
        override fun getDashLogger(uri: URI): SMAMeasurement = source
    }

    private fun createData(): SMAMeasurement {
        val subsection = hashMapOf(Pair("1", entries))
        val deviceData = SMADeviceData()
        deviceData.currentData = subsection
        val root = SMAMeasurement()
        root.result["device-id"] = deviceData
        return root
    }

    private fun getDevice(): Device {
        val device = Device()
        device.name = "test device"
        device.apiUrl = "http://localhost"
        return device
    }

    private fun getFirstMeasurement(): EnergyMeasurement {
        val measurement = EnergyMeasurement()
        measurement.timestamp = Date.from(Instant.now().minus(1, ChronoUnit.DAYS))
        return measurement
    }

    private fun loadData(): SMAMeasurement {
        val mapper = ObjectMapper()
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        return mapper.readValue(javaClass.classLoader.getResourceAsStream(MEASUREMENT_FILENAME), SMAMeasurement::class.java)
    }

    companion object {
        private const val MEASUREMENT_FILENAME = "sma_measurement_data.json"
    }
}