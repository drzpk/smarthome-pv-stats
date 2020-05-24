package dev.drzepka.pvstats.service.data

import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.repository.MeasurementRepository
import dev.drzepka.pvstats.util.Logger
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@Service
class MeasurementService(
        private val measurementRepository: MeasurementRepository
) {

    private val log by Logger()

    fun getAllForDay(device: Device, day: LocalDate): List<EnergyMeasurement> {
        val zonedTime = day.atStartOfDay().atZone(ZoneId.systemDefault())
        val from = Date.from(zonedTime.with(LocalTime.MIN).toInstant())
        val to = Date.from(zonedTime.with(LocalTime.MAX).toInstant())
        return measurementRepository.findForDateRange(device.id, from, to)
    }

    fun getFirstMeasurement(device: Device): EnergyMeasurement? {
        return measurementRepository.findFirstByDeviceIdOrderByTimestampAsc(device.id)
    }

    fun getLastMeasurement(device: Device): EnergyMeasurement {
        var last = measurementRepository.findLast(device.id)
        if (last == null) {
            last = EnergyMeasurement()
            last.timestamp = Date(0)
        }

        return last
    }

    /**
     * Stores multiple measurements. Only new measurements are saved (based on timestamp)
     */
    fun storeNewMeasurements(measurements: List<EnergyMeasurement>, lastStored: EnergyMeasurement): Boolean {
        // Assumption: everything is ordered by creation time
        // todo: try to optimize this. In most cases only a few new records will be added, so searching
        //       can be done backwards
        val indexOfFirst = measurements.indexOfFirst { it.timestamp.after(lastStored.timestamp) }
        if (indexOfFirst == -1) return false

        val subList = measurements.subList(indexOfFirst, measurements.size)
        measurementRepository.saveAll(subList)
        log.info("Saved ${subList.size} new measurement(s) for device ${lastStored.deviceId}")

        return true
    }
}