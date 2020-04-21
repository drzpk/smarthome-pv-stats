package dev.drzepka.pvstats.entity

import dev.drzepka.pvstats.model.DeviceType
import javax.annotation.Generated
import javax.persistence.*
import kotlin.random.Random

@Entity
class Device {
    @Id
    var id: Int = 0

    var name: String = ""
    var description: String? = null
    @Enumerated(EnumType.STRING)
    var type: DeviceType = DeviceType.UNKNOWN
    var active: Boolean = true

    var apiUrl: String = ""
}