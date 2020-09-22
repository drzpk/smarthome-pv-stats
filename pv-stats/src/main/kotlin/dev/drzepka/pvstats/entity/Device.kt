package dev.drzepka.pvstats.entity

import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import java.util.*
import javax.persistence.*

@Entity
class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    var name: String = ""
    var description: String? = null
    @Enumerated(EnumType.STRING)
    var type: DeviceType = DeviceType.GENERIC
    var createdAt = Date()
    var active: Boolean = true

    override fun toString(): String ="Device(id=$id, name=$name, type=$type, active=$active)"
}