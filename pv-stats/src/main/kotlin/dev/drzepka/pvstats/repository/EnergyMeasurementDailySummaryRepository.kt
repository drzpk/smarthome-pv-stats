package dev.drzepka.pvstats.repository

import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurementDailySummary
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EnergyMeasurementDailySummaryRepository : CrudRepository<EnergyMeasurementDailySummary, Int> {

    fun findFirstByDeviceOrderByCreatedAtDesc(device: Device): EnergyMeasurementDailySummary?
}