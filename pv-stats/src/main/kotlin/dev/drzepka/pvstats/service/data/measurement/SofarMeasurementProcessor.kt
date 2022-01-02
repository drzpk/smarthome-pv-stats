package dev.drzepka.pvstats.service.data.measurement

import dev.drzepka.pvstats.config.MeasurementConfig
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.model.InstantValue
import dev.drzepka.pvstats.service.DeviceDataService
import dev.drzepka.pvstats.service.data.MeasurementService
import dev.drzepka.pvstats.util.Logger
import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import dev.drzepka.smarthome.common.pvstats.model.vendor.sofar.SofarData
import dev.drzepka.smarthome.common.pvstats.model.vendor.sofar.SofarDataImpl
import dev.drzepka.smarthome.common.util.toLocalDate
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.util.*
import kotlin.math.floor

@Component
class SofarMeasurementProcessor(
        private val deviceDataService: DeviceDataService,
        private val measurementService: MeasurementService,
        private val measurementConfig: MeasurementConfig
) : MeasurementProcessor<SofarData>() {
    override val deviceType = DeviceType.SOFAR

    private val log by Logger()

    override fun process(device: Device, data: SofarData) {
        deviceDataService.set(device, DeviceDataService.Property.VENDOR_DATA, data.serialize() as String)

        val last = measurementService.getLastMeasurement(device, false)
        if (last != null && Instant.now().minusMillis(last.timestamp.toInstant().toEpochMilli()).toEpochMilli() < 50000) {
            log.trace("Data source for device ${device.id} tried to save new measurement too early")
            return
        }

        val new = if (last != null)
            createWithPriorMeasurement(device, last, data)
        else
            createsWithoutPriorMeasurement(device, data)

        measurementService.saveMeasurement(new)
    }

    override fun deserialize(data: Any): SofarDataImpl = SofarDataImpl.deserialize(data)

    private fun createWithPriorMeasurement(device: Device, last: EnergyMeasurement, data: SofarData): EnergyMeasurement {
        var previousDaily = deviceDataService.getInt(device, DeviceDataService.Property.DAILY_PRODUCTION)
        if (previousDaily?.instant?.toLocalDate()?.isBefore(LocalDate.now()) == true) {
            log.info("Daily production wasn't set today and will be ignored")
            previousDaily = null
        }

        val estimatedTotal = if (previousDaily != null && previousDaily.value <= data.energyToday) {
            // Normal case
            getEstimatedTotalProductionWh(last.totalWh, data.energyTotal, data.energyToday - previousDaily.value)
        } else if (previousDaily != null) {
            if (data.currentPower > 0) {
                log.warn("Data source for device ${device.id} is generating power, but inverter has switched to the next day")
                data.energyTotal
            } else {
                last.totalWh
            }
        } else {
            log.warn("No daily production found for device ${device.id}, exact value from logger will be used")
            data.energyTotal
        }

        val measurement = EnergyMeasurement()
        measurement.timestamp = Date()
        measurement.totalWh = estimatedTotal
        measurement.powerW = data.currentPower
        measurement.deviceId = last.deviceId
        calculateDeltaWh(previousDaily, data, last, measurement)

        deviceDataService.set(device, DeviceDataService.Property.DAILY_PRODUCTION, data.energyToday)
        return measurement
    }

    private fun calculateDeltaWh(previousDaily: InstantValue<Int>?, data: SofarData, last: EnergyMeasurement, current: EnergyMeasurement) {
        val timeDiff = floor((current.timestamp.time - last.timestamp.time) / 1000f + 0.5f).toInt()
        if (timeDiff > measurementConfig.maxAllowedIntervalSeconds) {
            log.info("Interval between last and current measurement for device ${current.deviceId} is too big, " +
                    "DeltaWh will be set to zero. ($timeDiff > ${measurementConfig.maxAllowedIntervalSeconds})")
            current.deltaWh = 0
            return
        }

        current.deltaWh = if (previousDaily != null && previousDaily.value <= data.energyToday) {
            // Accurate delta is available
            data.energyToday - previousDaily.value
        } else {
            var d = current.totalWh - last.totalWh
            if (d < 0) {
                log.trace("DeltaWh for measurement at ${current.timestamp} was negative and had to be trimmed to zero")
                d = 0
            }
            d
        }
    }

    private fun createsWithoutPriorMeasurement(device: Device, data: SofarData): EnergyMeasurement {
        val measurement = EnergyMeasurement()
        measurement.timestamp = Date()
        measurement.totalWh = data.energyTotal
        measurement.deltaWh = 0
        measurement.powerW = data.currentPower
        measurement.deviceId = device.id

        deviceDataService.set(device, DeviceDataService.Property.DAILY_PRODUCTION, data.energyToday)

        return measurement
    }

    /**
     * Total production in some (all?) Sofar devices is returned with 1 kWh accuraccy.
     * This function's taks is to estimate real total production with worst-case-scenario accuracy 1 kWh.
     */
    @Suppress("UnnecessaryVariable")
    internal fun getEstimatedTotalProductionWh(previousTotalGuessed: Int, currentTotalReal: Int, delta: Int): Int {
        fun compareTo1kWh(left: Int, right: Int) = floor(left / 1000f).toInt() - floor(right / 1000f).toInt()

        val currentTotalGuessed = previousTotalGuessed + delta
        when (compareTo1kWh(currentTotalGuessed, currentTotalReal)) {
            0 -> {
                // Best case scenario: prediction is good enough
                return currentTotalGuessed
            }
            -1 -> {
                // Guessed total production is lower than real
                val maxExclusiveReal = currentTotalReal
                val minExclusiveReal = maxExclusiveReal - delta
                return minExclusiveReal + floor((maxExclusiveReal - minExclusiveReal) / 2f).toInt() + delta
            }
            1 -> {
                // Guessed total production is higher than real
                val minExclusiveReal = currentTotalReal
                val maxExclusiveReal = minExclusiveReal + 1000 - delta
                return minExclusiveReal + floor((maxExclusiveReal - minExclusiveReal) / 2f).toInt() + delta
            }
            else -> {
                log.warn("Difference between previous guessed and current real total production is too big, setting " +
                        "estimation to (inaccurate) real value")
                return currentTotalReal
            }
        }
    }

}