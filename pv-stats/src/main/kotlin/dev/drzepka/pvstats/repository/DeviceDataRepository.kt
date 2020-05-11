package dev.drzepka.pvstats.repository

import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.DeviceData
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DeviceDataRepository : CrudRepository<DeviceData, Date> {
    fun findByDeviceIdAndProperty(deviceId: Int, property: String): DeviceData?
    fun deleteByDeviceAndProperty(device: Device, property: String)

    @Modifying
    @Query("update DeviceData d set d.value = :value where d.device.id = :deviceId and d.property = :property")
    fun replaceValue(@Param("deviceId") deviceId: Int, @Param("property") proeprty: String, @Param("value") value: String): Int
}