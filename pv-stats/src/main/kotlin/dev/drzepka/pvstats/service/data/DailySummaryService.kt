package dev.drzepka.pvstats.service.data

import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurementDailySummary
import dev.drzepka.pvstats.repository.EnergyMeasurementDailySummaryRepository
import dev.drzepka.pvstats.service.DeviceService
import dev.drzepka.pvstats.util.Logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId

/**
 * Creates summaries for data from *previous days*.
 */
@Service
class DailySummaryService(
        private val deviceService: DeviceService,
        private val measurementService: MeasurementService,
        private val energyMeasurementDailySummaryRepository: EnergyMeasurementDailySummaryRepository,
        private val handlerResolverService: HandlerResolverService
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

    private fun createMissingSummaries(device: Device) {
        var startFromDate = getLastSummaryDateFor(device)

        if (startFromDate == null) {
            // No summaries yet, start calculations from a day the first measurement was registered
            startFromDate = getFirstMeasurementDateFor(device)
            if (startFromDate == null) {
                log.warn("Skipping summary for device ${device.id} (${device.name}) - no data available")
                return
            }
        } else {
            startFromDate = startFromDate.plusDays(1)
        }

        val yesterday = LocalDate.now().minusDays(1)
        while (!startFromDate!!.isAfter(yesterday)) {
            createSummaryForDay(device, startFromDate)
            startFromDate = startFromDate.plusDays(1)
        }
    }

    private fun createSummaryForDay(device: Device, day: LocalDate) {
        log.info("Creating daily energy summary for device ${device.id} (${device.name}) for $day")

        val processor = handlerResolverService.summary(device.type)
        if (processor == null) {
            log.info("No summary processor for device type ${device.type} found")
            return
        }

        // Note: this method doesn't return one more record that's the first one in the next day,
        // so delta Wh won't be accurate.
        // Well, actually it will, because daily summary job will be happening at midnight
        // and at that time it's usually dark so I'll turn a blind eye here.
        val data = measurementService.getAllForDay(device, day)

        // Getting most recent summary for given device will always work, since
        // all summaries are created sequentially
        val last = getLastSummaryFor(device)

        val summary = EnergyMeasurementDailySummary()
        summary.createdAt = day
        summary.device = device
        processor.calculateSummary(last, summary, data)
        energyMeasurementDailySummaryRepository.save(summary)
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