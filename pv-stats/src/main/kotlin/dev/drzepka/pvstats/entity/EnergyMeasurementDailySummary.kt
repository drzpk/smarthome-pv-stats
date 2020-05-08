package dev.drzepka.pvstats.entity

import java.time.LocalDate
import javax.persistence.*

@Entity
class EnergyMeasurementDailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id = 0
    var createdAt = LocalDate.now()!!
    var totalWh = 0
    var deltaWh = 0
    var avgPower = 0f
    var maxPower = 0
    @ManyToOne
    var device = Device()
}