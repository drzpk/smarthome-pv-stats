package dev.drzepka.pvstats.repository

import dev.drzepka.pvstats.entity.EnergyMeasurement
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MeasurementRepository : CrudRepository<EnergyMeasurement, Date> {

    @Query("select * from energy_measurement order by timestamp desc limit 1", nativeQuery = true)
    fun findLast(): EnergyMeasurement? // TODO: delete this (doesn't take device id into account)

    @Query("select * from energy_measurement where device_id = ?1 order by timestamp desc limit 1", nativeQuery = true)
    fun findLast(deviceId: Int): EnergyMeasurement?
}