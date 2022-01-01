package dev.drzepka.pvstats.repository

import dev.drzepka.pvstats.entity.Device
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceRepository : CrudRepository<Device, Int> {

    fun findByActive(active: Boolean): List<Device>

    fun findByName(name: String): Device?
}