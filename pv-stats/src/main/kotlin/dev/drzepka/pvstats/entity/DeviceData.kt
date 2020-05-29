package dev.drzepka.pvstats.entity

import org.hibernate.annotations.UpdateTimestamp
import java.util.*
import javax.persistence.*

@Entity
class DeviceData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id = 0
    var property = ""
    var value = ""
    @UpdateTimestamp
    var updatedAt = Date()
    @ManyToOne
    var device = Device()
}