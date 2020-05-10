package dev.drzepka.pvstats.repository

import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.DeviceData
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DeviceDataService : CrudRepository<DeviceData, Date> {
    fun findByDeviceIdAndProperty(deviceId: Int, property: String): DeviceData?
    fun deleteByDeviceAndProperty(device: Device, property: String)
    @Query("update device_cache set `value` = ?3 where device_id = ?1 and property = ?2", nativeQuery = true)
    fun replaceValue(deviceId: Int, proeprty: String, value: String): Int
}