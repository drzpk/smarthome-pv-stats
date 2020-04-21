package dev.drzepka.pvstats.service.connector

import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.model.DeviceType
import dev.drzepka.pvstats.model.device.sma.Entry
import dev.drzepka.pvstats.util.Logger
import dev.drzepka.pvstats.web.client.sma.SMAApiClient
import org.springframework.stereotype.Service
import java.net.URI

@Service
class SMAConnector(private val smaClient: SMAApiClient) : Connector {
    override val type = DeviceType.SMA

    private val log by Logger()

    override fun collectMeasurements(device: Device, lastMeasurement: EnergyMeasurement): List<EnergyMeasurement> {
        val entries = getEntries(device) ?: return emptyList()
        if (entries.isEmpty()) {
            log.warn("No entries for device {}", device.name)
            return emptyList()
        }

        val measurements = ArrayList<EnergyMeasurement>()
        measurements.ensureCapacity(entries.size)

        measurements.add(createMeasurement(lastMeasurement, entries.first(), device))
        for (i in 1 until entries.size) {
            val newMeasurement = createMeasurement(measurements[i - 1], entries[i], device)
            newMeasurement.deviceId = device.id
            measurements.add(newMeasurement)
        }

        return measurements
    }

    private fun createMeasurement(first: EnergyMeasurement, second: Entry, device: Device): EnergyMeasurement {
        val measurement = EnergyMeasurement()
        measurement.timestamp = second.t
        measurement.totalWh = second.v ?: first.totalWh
        measurement.deltaWh = measurement.totalWh - first.totalWh
        measurement.deviceId = device.id
        return measurement
    }

    private fun getEntries(device: Device): List<Entry>? {
        val rawData = smaClient.getDashLogger(URI.create(device.apiUrl))

        val deviceKey = rawData.result.keys.firstOrNull()
        if (deviceKey == null) {
            log.warn("Device key is null for device {}", device.name)
            return null
        }

        val deviceData = rawData.result[deviceKey]
        val currentData = deviceData!!.currentData

        val subsection1 = currentData[TODAY_MEASUREMENTS_SUBKEY]
        if (subsection1 == null) {
            log.warn("Missing list subsection 1 for device {}", device.name)
            return null
        }

        return subsection1
    }

    companion object {
        private const val TODAY_MEASUREMENTS_SUBKEY = "1" // device class
    }
}