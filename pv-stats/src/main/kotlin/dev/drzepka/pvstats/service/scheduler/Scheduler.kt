package dev.drzepka.pvstats.service.scheduler

import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.repository.DeviceRepository
import dev.drzepka.pvstats.repository.MeasurementRepository
import dev.drzepka.pvstats.service.ConnectorFactory
import dev.drzepka.pvstats.util.Logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class Scheduler(
        private val deviceRepository: DeviceRepository,
        private val connectorFactory: ConnectorFactory,
        private val measurementRepository: MeasurementRepository) {

    private val log by Logger()

    @Scheduled(cron = "\${scheduler.measurement-collection}")
    fun collectMeasurements() {
        log.info("Starting measurement collection")

        val activeDevices = deviceRepository.findByActive(true)
        activeDevices.forEach {
            val connector = connectorFactory.getConnector(it.type)
            val lastStoredMeasuremnt = getLastMeasurement(it)
            val measurements = connector.collectMeasurements(it, lastStoredMeasuremnt)

            if (!storeNewMeasurements(measurements, lastStoredMeasuremnt))
                log.info("No new measurements for device ${it.name}")
        }
    }

    private fun getLastMeasurement(device: Device): EnergyMeasurement {
        var last = measurementRepository.findLast(device.id)
        if (last == null) {
            last = EnergyMeasurement()
            last.timestamp = Date(0)
        }

        return last
    }

    private fun storeNewMeasurements(measurements: List<EnergyMeasurement>, lastStored: EnergyMeasurement): Boolean {
        // Assumption: everything is ordered by creation time
        val indexOfFirst = measurements.indexOfFirst { it.timestamp.after(lastStored.timestamp) }
        if (indexOfFirst == -1) return false

        val subList = measurements.subList(indexOfFirst, measurements.size)
        measurementRepository.saveAll(subList)
        log.info("Saved ${subList.size} new measurement(s)")

        return true
    }
}