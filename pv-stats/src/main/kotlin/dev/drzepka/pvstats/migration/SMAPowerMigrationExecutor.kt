package dev.drzepka.pvstats.migration

import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.model.DeviceType
import dev.drzepka.pvstats.repository.DeviceRepository
import dev.drzepka.pvstats.repository.MeasurementRepository
import dev.drzepka.pvstats.util.Logger
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*
import kotlin.math.floor

/**
 * Fill power values for SMA, because they weren't collected before.
 */
class SMAPowerMigrationExecutor(
        private val deviceRepository: DeviceRepository,
        private val measurementRepository: MeasurementRepository
) : MigrationExecutor {
    override val name = "SMAPower"

    private val log by Logger()

    override fun execute() {
        deviceRepository.findAll().forEach {
            if (it.type != DeviceType.SMA) {
                log.trace("Skipping not suitable device: $it")
            }
            executeForDevice(it)
        }
    }

    private fun executeForDevice(device: Device) {
        log.info("Starting power migration for device $device")

        val firstRecord = measurementRepository.findFirstByDeviceIdOrderByTimestampAsc(device.id)
        if (firstRecord == null) {
            log.info("Device $device doesn't have any data")
            return
        }

        var currentDay = firstRecord.timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        var data: List<EnergyMeasurement>
        do {
            log.info("Migarting day: $currentDay")
            val from = Date.from(currentDay.atStartOfDay(ZoneId.systemDefault()).toInstant())
            val to = Date.from(currentDay.atStartOfDay(ZoneId.systemDefault()).with(LocalTime.MAX).toInstant())
            data = measurementRepository.findForDateRange(device.id, from, to)
            executeForData(data)

            // Checkpoint
            measurementRepository.saveAll(data)

            currentDay = currentDay.plusDays(1)
        } while (data.isNotEmpty() || !currentDay.isAfter(LocalDate.now()))
    }

    private fun executeForData(data: List<EnergyMeasurement>) {
        for (i in 0 until (data.size - 1)) {
            val first = data[i]
            val second = data[i + 1]

            val hourDiff = (second.timestamp.time - first.timestamp.time) / 3_600_000.0
            second.powerW = floor(second.deltaWh / hourDiff + 0.5f).toInt()
        }
    }
}