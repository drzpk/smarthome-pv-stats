package dev.drzepka.pvstats.entity

import javax.persistence.*

@Entity
class DeviceData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id = 0
    var property = ""
    var value = ""
    @ManyToOne
    var device = Device()
}