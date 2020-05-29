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
    /**
     * Watt hours change since lat measuremnt.
     *
     * **Note**: this variable is used solely in graphing and you **MUSN'T** use it in internal calculations because
     * it can be set to zero on some occassions.
     */
    var deltaWh: Int = 0
    @Column(name = "power_w")
    var powerW: Int = 0
    var deviceId: Int = 0
}