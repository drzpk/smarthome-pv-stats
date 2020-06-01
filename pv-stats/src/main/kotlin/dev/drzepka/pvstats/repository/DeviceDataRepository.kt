package dev.drzepka.pvstats.repository

import dev.drzepka.pvstats.entity.DeviceData
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DeviceDataRepository : CrudRepository<DeviceData, Date> {
    @Query("select d from DeviceData d where d.key.device.id = :deviceId and d.key.property = :property")
    fun findByDeviceIdAndProperty(deviceId: Int, property: String): DeviceData?
}