package dev.drzepka.pvstats.service.data

import dev.drzepka.pvstats.autoconfiguration.CachingAutoConfiguration
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.repository.MeasurementRepository
import dev.drzepka.pvstats.util.Logger
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*
import javax.cache.CacheManager
import javax.persistence.EntityManager
import javax.transaction.Transactional

@Service
class MeasurementService(
        private val measurementRepository: MeasurementRepository,
        private val entityManager: EntityManager,
        cacheManager: CacheManager
) {

    private val log by Logger()
    private val lastMeasurementCache = cacheManager.getCache<Int, EnergyMeasurement>(CachingAutoConfiguration.CACHE_LAST_MEASUREMENTS)
    private val secondLastMeasurementCache = cacheManager.getCache<Int, EnergyMeasurement>(CachingAutoConfiguration.CACHE_SECOND_LAST_MEASUREMENTS)

    fun getAllForDay(device: Device, day: LocalDate): List<EnergyMeasurement> {
        val zonedTime = day.atStartOfDay().atZone(ZoneId.systemDefault())
        val from = Date.from(zonedTime.with(LocalTime.MIN).toInstant())
        val to = Date.from(zonedTime.with(LocalTime.MAX).toInstant())
        return measurementRepository.findForDateRange(device.id, from, to)
    }

    fun getFirstMeasurement(device: Device): EnergyMeasurement? {
        return measurementRepository.findFirstByDeviceIdOrderByTimestampAsc(device.id)
    }

    fun getLastMeasurement(device: Device, returnEmptyIfDoesntExist: Boolean = true): EnergyMeasurement? {
        var last = lastMeasurementCache.get(device.id) ?: measurementRepository.findLast(device.id)

        if (last == null && returnEmptyIfDoesntExist) {
            last = EnergyMeasurement()
            last.timestamp = Date(0)
        }

        return last
    }

    @Transactional
    fun saveMeasurement(measurement: EnergyMeasurement) {
        if (log.isTraceEnabled)
            log.trace("Saving $measurement")

        val idToUpdate = getMeasurementIdToUpdate(measurement)
        if (idToUpdate != null) {
            if (log.isTraceEnabled)
                log.trace("Current measurement for device ${measurement.deviceId} matches with the previous one and " +
                        "will be overridden (totalWh: ${measurement.totalWh})")
            measurement.id = idToUpdate

            try {
                entityManager.merge(measurement)
            } catch (e: Exception) {
                log.error("An error occurred during an attempt to merge existing measurement $idToUpdate with $measurement. " +
                        "Measurement will be saved as a new record.")
                measurementRepository.save(measurement)
            }
        } else {
            measurementRepository.save(measurement)
        }

        cacheNewMeasurement(measurement)
    }

    /**
     * Stores multiple measurements. Only new measurements are saved (based on timestamp). This method is supposed to
     * be called by processors of devices that return all records from last X hours every time it is called.
     */
    fun saveMeasurements(measurements: List<EnergyMeasurement>, lastStored: EnergyMeasurement) {
        // Assumption: everything is ordered by creation time
        // todo: Measurement logic is also present in this method's caller. This should fixed.

        var allMeasurementsIdentical = true
        var indexOfFirst = -1

        for (i in measurements.indices) {
            if (!allMeasurementsIdentical && indexOfFirst > -1) break

            if (indexOfFirst == -1 && measurements[i].timestamp.after(lastStored.timestamp))
                indexOfFirst = i
            if (allMeasurementsIdentical && !lastStored.isValueIdentical(measurements[i]))
                allMeasurementsIdentical = false
        }

        if (indexOfFirst == -1) {
            return
        }
        if (allMeasurementsIdentical) {
            log.debug("All new measurements for device ${lastStored.deviceId} are identical with the one previously stored. " +
                    "Waiting for more measurements before saving them to the database.")
            return
        }

        val newMeasurements = LinkedList<EnergyMeasurement>(measurements.subList(indexOfFirst, measurements.size))
        val removed = removeDuplicatedMeasurements(newMeasurements, lastStored)
        measurementRepository.saveAll(newMeasurements)

        if (newMeasurements.isNotEmpty())
            cacheNewMeasurement(newMeasurements.last)

        log.info("Saved ${newMeasurements.size} new measurement(s) for device ${lastStored.deviceId}. Removed duplicates: $removed")
    }

    private fun getMeasurementIdToUpdate(measurementToBeStored: EnergyMeasurement): Int? {
        val secondLast = secondLastMeasurementCache.get(measurementToBeStored.deviceId) ?: return null
        val last = lastMeasurementCache.get(measurementToBeStored.deviceId) ?: return null

        return if (secondLast.isValueIdentical(last) && last.isValueIdentical(measurementToBeStored))
            last.id
        else
            null
    }

    private fun removeDuplicatedMeasurements(measurements: LinkedList<EnergyMeasurement>, first: EnergyMeasurement): Int {
        if (measurements.isEmpty()) return 0

        val iterator = measurements.listIterator()
        var previous = first
        var current = iterator.next()
        var removed = 0

        // Remove all middle repeated elements leaving first and last ones
        while (true) {
            val next = if (iterator.hasNext()) iterator.next() else break

            if (previous.isValueIdentical(current) && current.isValueIdentical(next)) {
                iterator.previous() // First call returns the same value as iterator.next()
                iterator.previous()
                iterator.remove()
                iterator.next()
                removed++
            }

            previous = current
            current = next
        }

        return removed
    }

    private fun cacheNewMeasurement(measurement: EnergyMeasurement) {
        val deviceId = measurement.deviceId

        val existing = lastMeasurementCache.get(deviceId)
        if (existing != null) {
            secondLastMeasurementCache.put(deviceId, existing)
        } else {
            log.trace("No last measurement cache hit for deviceId $deviceId")
            secondLastMeasurementCache.remove(deviceId)
        }

        lastMeasurementCache.put(deviceId, measurement)
    }
}