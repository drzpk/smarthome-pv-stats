package dev.drzepka.pvstats.entity

import java.util.*
import javax.persistence.*

@Suppress("unused")
@Entity
class Migration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id = 0
    var name = ""
    var executedAt = Date()
    var executionTimeMs = 0
    @Column(columnDefinition = "tinyint(1)")
    var status = true

    override fun toString(): String = "Migration(id=$id, name=$name)"
}