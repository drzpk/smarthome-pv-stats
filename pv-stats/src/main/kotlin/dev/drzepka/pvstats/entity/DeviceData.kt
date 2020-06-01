package dev.drzepka.pvstats.entity

import org.hibernate.annotations.UpdateTimestamp
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
class DeviceData {
    @EmbeddedId
    var key = Key()
    var value = ""
    @UpdateTimestamp
    var updatedAt = Date()

    @get:Transient
    var device: Device
        get() = key.device
        set(value) {
            key.device = value
        }

    @get:Transient
    var property: String
        get() = key.property
        set(value) {
            key.property = value
        }

    @Embeddable
    class Key : Serializable {
        @ManyToOne
        var device = Device()
        var property = ""

        override fun equals(other: Any?): Boolean = other is Key && other.device.equals(this.device) && other.property == this.property

        override fun hashCode(): Int = Objects.hash(device.id, property)
    }
}