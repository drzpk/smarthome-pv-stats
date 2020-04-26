package dev.drzepka.pvstats.entity

import dev.drzepka.pvstats.model.DeviceType
import java.util.*
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
class Device {
    @Id
    var id: Int = 0

    var name: String = ""
    var description: String? = null
    @Enumerated(EnumType.STRING)
    var type: DeviceType = DeviceType.UNKNOWN
    var createdAt = Date()
    var active: Boolean = true

    var apiUrl: String = ""
}