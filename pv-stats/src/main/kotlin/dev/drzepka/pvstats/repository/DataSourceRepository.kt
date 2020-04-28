package dev.drzepka.pvstats.repository

import dev.drzepka.pvstats.entity.DataSource
import dev.drzepka.pvstats.entity.Device
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DataSourceRepository : CrudRepository<DataSource, Int> {
    fun findByDevice(device: Device): DataSource?
    fun findByUser(user: String): DataSource?
}