package dev.drzepka.pvstats.service.data

import dev.drzepka.pvstats.common.model.vendor.DeviceType
import dev.drzepka.pvstats.common.model.vendor.SofarData
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.entity.EnergyMeasurementDailySummary
import dev.drzepka.pvstats.repository.EnergyMeasurementDailySummaryRepository
import dev.drzepka.pvstats.service.DeviceDataService
import dev.drzepka.pvstats.service.DeviceService
import dev.drzepka.pvstats.util.Logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.floor
import kotlin.math.max

@Service
class DailySummaryService(
        private val deviceService: DeviceService,
        private val measurementService: MeasurementService,
        private val energyMeasurementDailySummaryRepository: EnergyMeasurementDailySummaryRepository,
        private val deviceDataService: DeviceDataService
) {

    private val log by Logger()

    @Synchronized
    @Scheduled(cron = "\${scheduler.daily-summary}")
    fun createSummary() {
        log.info("Creating energy daily summary")
        deviceService.getActiveDevices().forEach {
            createMissingSummaries(it)
        }
    }

    fun getLastSummaryFor(device: Device): EnergyMeasurementDailySummary? {
        return energyMeasurementDailySummaryRepository.findFirstByDeviceOrderByCreatedAtDesc(device)
    }

    internal fun createMissingSummaries(device: Device) {
        var current = getLastSummaryDateFor(device)

        if (current == null) {
            // No summaries yet, start calculations from a day the first measurement was registered
            current = getFirstMeasurementDateFor(device)
            if (current == null) {
                log.warn("Skipping summary for device ${device.id} (${device.name}) - no data available")
                return
            }
        } else {
            current = current.plusDays(1)
        }

        val yesterday = LocalDate.now().minusDays(1)
        while (!current!!.isAfter(yesterday)) {
            createSummaryForDay(device, current)
            current = current.plusDays(1)
        }
    }

    internal fun createSummaryForDay(device: Device, day: LocalDate) {
        log.debug("Creating daily energy summary for device ${device.id} (${device.name}) for $day")

        val summary = EnergyMeasurementDailySummary()
        summary.createdAt = day
        summary.device = device

        // Note: this method doesn't return one more record that's the first one in the next day,
        // so delta Wh won't be accurate.
        // Well, actually it will, because daily summary job will be happening at midnight
        // and at that time it's usually dark so I'll turn a blind eye here.
        val data = measurementService.getAllForDay(device, day)
        if (device.type == DeviceType.SOFAR)
            calculateSofarSummary(summary, data, device) // todo: create migration script for power column and delete this special-case method
        else
            calculateSummary(summary, data)
        energyMeasurementDailySummaryRepository.save(summary)
    }

    internal fun calculateSummary(summary: EnergyMeasurementDailySummary, data: List<EnergyMeasurement>) {
        val previousTotalWh = getLastSummaryFor(summary.device)?.totalWh ?: 0
        summary.totalWh = if (data.isNotEmpty()) data.last().totalWh else previousTotalWh
        summary.deltaWh = summary.totalWh - previousTotalWh

        var nonZeroPowerSum = 0.0
        var nonZeroPowerCount = 0
        var maxPower = 0.0

        for (i in 0 until (data.size - 1)) {
            val left = data[i]
            val right = data[i + 1]
            val deltaWh = right.totalWh - left.totalWh
            if (deltaWh > 0) {
                val deltaHours = (right.timestamp.time - left.timestamp.time) / 3_600_000.0
                if (deltaHours == 0.0) {
                    log.warn("Energy measurement records ${left.id} and ${right.id} have the same time, skipping diff calculation")
                    continue
                }

                val power = deltaWh / deltaHours
                if (power.isNaN() || power.isInfinite()) {
                    log.warn("Power calculation for energy measurement records ${left.id} and ${right.id} is invalid; " +
                            "power = $power, deltaWh = $deltaWh; deltaHours = $deltaHours")
                    continue
                }

                nonZeroPowerSum += power
                nonZeroPowerCount++
                maxPower = max(maxPower, power)
            }
        }

        summary.maxPower = floor(maxPower + 0.5).toInt()
        summary.avgPower = if (nonZeroPowerCount > 0) (nonZeroPowerSum / nonZeroPowerCount).toFloat() else 0f
    }

    @Suppress("UNCHECKED_CAST")
    internal fun calculateSofarSummary(summary: EnergyMeasurementDailySummary, data: List<EnergyMeasurement>, device: Device) {
        val previousTotalWh = getLastSummaryFor(summary.device)?.totalWh ?: 0
        summary.totalWh = if (data.isNotEmpty()) data.last().totalWh else previousTotalWh

        val rawData = deviceDataService.getBytes(device, DeviceDataService.Property.VENDOR_DATA, true)
        if (rawData != null) {
            // Use more accurate data
            val sofarData = SofarData(rawData.toTypedArray())
            summary.deltaWh = sofarData.energyToday
        } else {
            summary.deltaWh = summary.totalWh - previousTotalWh
        }

        var nonZeroPowerSum = 0.0
        var nonZeroPowerCount = 0
        var maxPower = 0

        data.forEach {
            if (it.powerW > 0) {
                nonZeroPowerSum += it.powerW
                nonZeroPowerCount++
                maxPower = max(maxPower, it.powerW)
            }
        }

        summary.maxPower = maxPower
        summary.avgPower = if (nonZeroPowerCount > 0) (nonZeroPowerSum / nonZeroPowerCount).toFloat() else 0f
    }

    internal fun getLastSummaryDateFor(device: Device): LocalDate? {
        val last = getLastSummaryFor(device)
        return last?.createdAt
    }

    private fun getFirstMeasurementDateFor(device: Device): LocalDate? {
        val first = measurementService.getFirstMeasurement(device)
        return first?.timestamp?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
    }
}