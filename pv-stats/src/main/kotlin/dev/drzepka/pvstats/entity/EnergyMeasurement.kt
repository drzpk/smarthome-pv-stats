package dev.drzepka.pvstats.entity

import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class EnergyMeasurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0
    var timestamp: Date = Date()

    var totalWh: Int = 0 // Until now
    var deltaWh: Int = 0 // Since last measurement
    var deviceId: Int = 0
}