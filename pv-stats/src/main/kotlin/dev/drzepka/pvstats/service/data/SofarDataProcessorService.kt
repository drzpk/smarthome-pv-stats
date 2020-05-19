package dev.drzepka.pvstats.service.data

import dev.drzepka.pvstats.autoconfiguration.CachingAutoConfiguration
import dev.drzepka.pvstats.common.model.vendor.SofarData
import dev.drzepka.pvstats.common.model.vendor.VendorType
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.repository.MeasurementRepository
import dev.drzepka.pvstats.service.DeviceDataService
import dev.drzepka.pvstats.util.Logger
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import javax.cache.CacheManager
import kotlin.math.floor

@Service
class SofarDataProcessorService(
        private val deviceDataService: DeviceDataService,
        private val measurementRepository: MeasurementRepository,
        cacheManager: CacheManager
) : DataProcessorService<SofarData>(SofarData::class, deviceDataService) {
    override val vendorType = VendorType.SOFAR

    private val lastMeasurementCache = cacheManager.getCache<Any, Any>(CachingAutoConfiguration.CACHE_LAST_MEASUREMENTS)

    private val log by Logger()

    override fun process(device: Device, data: SofarData) {
        val last = getLastMeasurment(device)
        if (last != null && Instant.now().minusMillis(last.timestamp.toInstant().toEpochMilli()).toEpochMilli() < 50000) {
            log.trace("Data source for device ${device.id} tried to save new measurement too early")
            return
        }

        val new = if (last != null)
            createWithPriorMeasurement(device, last, data)
        else
            createsWithoutPriorMeasurement(device, data)

        saveMeasurement(device, new)
    }

    private fun createWithPriorMeasurement(device: Device, last: EnergyMeasurement, data: SofarData): EnergyMeasurement {
        val previousDaily = deviceDataService.getInt(device, DeviceDataService.Property.DAILY_PRODUCTION)
        val estimatedTotal = if (previousDaily != null && previousDaily <= data.energyToday) {
            // Normal case
            getEstimatedTotalProductionWh(last.totalWh, data.energyTotal, data.energyToday - previousDaily)
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

        measurement.deltaWh = if (previousDaily != null && previousDaily <= data.energyToday) {
            // Accurate delta is available
            data.energyToday - previousDaily
        } else {
            var d = measurement.totalWh - last.totalWh
            if (d < 0) {
                log.trace("DeltaWh for measurement at ${measurement.timestamp} was negative and had to be trimmed to zero")
                d = 0
            }
            d
        }

        deviceDataService.set(device, DeviceDataService.Property.DAILY_PRODUCTION, data.energyToday)
        return measurement
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

    private fun getLastMeasurment(device: Device): EnergyMeasurement? {
        var last = lastMeasurementCache
                .get(device.id) as EnergyMeasurement?

        if (last == null)
            last = measurementRepository.findLast(device.id)

        return last
    }

    private fun saveMeasurement(device: Device, measurement: EnergyMeasurement) {
        measurementRepository.save(measurement)
        lastMeasurementCache.put(device.id, measurement)
    }

}