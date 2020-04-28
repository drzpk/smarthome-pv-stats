package dev.drzepka.pvstats.entity

import java.util.*
import javax.persistence.*

@Entity
class DataSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0
    var user: String = ""
    var password: String = ""
    @OneToOne
    var device: Device? = null
    var createdAt: Date = Date()
    var updatedAtVersion: Int = 0

    override fun toString(): String = "DataSource (id=$id, user=$user, device=${device?.id
            ?: -1}, createdAt=$createdAt, updatedAtVersion=$updatedAtVersion)"
}