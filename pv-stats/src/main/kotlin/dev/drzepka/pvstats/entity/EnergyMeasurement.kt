package dev.drzepka.pvstats.entity

import java.util.*
import javax.persistence.*

@Entity
class EnergyMeasurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0
    var timestamp: Date = Date()

    var totalWh: Int = 0 // Until now
    var deltaWh: Int = 0 // Since last measurement
    @Column(name = "power_w")
    var powerW: Int = 0
    var deviceId: Int = 0
}