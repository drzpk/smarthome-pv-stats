package dev.drzepka.pvstats.service.data.measurement

import dev.drzepka.pvstats.config.MeasurementConfig
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.service.DeviceDataService
import dev.drzepka.pvstats.service.data.MeasurementService
import dev.drzepka.pvstats.util.Logger
import dev.drzepka.smarthome.common.pvstats.model.sma.Entry
import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import dev.drzepka.smarthome.common.pvstats.model.vendor.SMAData
import org.springframework.stereotype.Component
import java.util.*
import kotlin.math.floor

@Component
class SMAMeasurementProcessor(
        private val measurementService: MeasurementService,
        private val deviceDataService: DeviceDataService,
        private val measurementConfig: MeasurementConfig
) : MeasurementProcessor<SMAData>() {
    override val deviceType = DeviceType.SMA

    private val log by Logger()

    // todo: mechanism for DoS detection and mitigation
    override fun process(device: Device, data: SMAData) {
        if (data.dashValues != null)
            deviceDataService.set(device, DeviceDataService.Property.POWER, data.dashValues!!.getPower())

        val entries = getEntries(device, data)
        if (entries == null) {
            log.trace("No data to process for device $device")
            return
        } else if (entries.isEmpty()) {
            log.warn("No entries for device {}", device.name)
            return
        }

        val lastMeasurement = measurementService.getLastMeasurement(device)!!

        // Most of the time only a few most recent records will be added as measurements, so start from the end
        var startFrom = 0
        for (i in entries.size - 1 downTo 0) {
            if (entries[i].t == lastMeasurement.timestamp) {
                startFrom = i + 1
                break
            }

            if (entries[i].t.before(lastMeasurement.timestamp)) {
                if (i > 0) {
                    log.warn("Possible data inconsistency detected for device $device. " +
                            "There's no common element between new and existing measurement entries.")
                }
                startFrom = i + 1
                break
            }
        }

        if (startFrom == entries.size) {
            // No new entries
            return
        }

        val measurements = ArrayList<EnergyMeasurement>()
        measurements.ensureCapacity(entries.size - startFrom)
        val firstNewMeasurement = createMeasurement(lastMeasurement, entries[startFrom], device)
        if (firstNewMeasurement != null)
            measurements.add(firstNewMeasurement)

        for (i in startFrom + 1 until entries.size) {
            val newMeasurement = createMeasurement(measurements[i - startFrom - 1], entries[i], device)
            if (newMeasurement == null) {
                log.error("Only first measurement is expected to be invalid and missing")
                continue
            }
            newMeasurement.deviceId = device.id
            measurements.add(newMeasurement)
        }

        measurementService.saveMeasurements(measurements, lastMeasurement)
    }

    override fun deserialize(data: Any): SMAData = SMAData.deserialize(data)

    private fun createMeasurement(first: EnergyMeasurement, second: Entry, device: Device): EnergyMeasurement? {
        val measurement = EnergyMeasurement()
        measurement.timestamp = second.t
        measurement.totalWh = second.v ?: first.totalWh
        measurement.deviceId = device.id

        val deltaHours = (second.t.time - first.timestamp.time) / 3_600_000f
        if (deltaHours <= 0f)
            throw IllegalStateException("Delta time for device $device isn't positive")

        val deltaSeconds = floor((second.t.time - first.timestamp.time) / 1_000f + 0.5f).toInt()
        measurement.deltaWh = if (deltaSeconds < measurementConfig.maxAllowedIntervalSeconds)
            measurement.totalWh - first.totalWh
        else
            0

        if (measurement.deltaWh < 0) {
            // Sometimes SMA returns chart data with null "v" values in the middle of a day
            // and first non-null value is far smaller than it should be. No idea what it is happening,
            // the only reasonable solution is to ignore such measurements.
            log.warn("Cannot create measurement for device ${device.id} (${device.name}): " +
                    "new measurement's totalWh value (${measurement.totalWh}) is smaller than " +
                    "last measurement's ($first)")
            return null
        }

        var power = measurement.deltaWh / deltaHours
        if (power.isNaN() || power.isInfinite()) {
            log.warn("Power calculation based energy measurement record ${first.id} is invalid; " +
                    "power = $power, deltaWh = ${measurement.deltaWh}; deltaHours = $deltaHours")
            power = 0f
        }
        measurement.powerW = floor(power + 0.5f).toInt()

        return measurement
    }

    private fun getEntries(device: Device, data: SMAData): List<Entry>? {
        return try {
            data.measurement?.getEntries()
        } catch (e: Exception) {
            log.error("Error while trying to extract measurement data for device ${device.name}: ${e.message}")
            null
        }
    }
}