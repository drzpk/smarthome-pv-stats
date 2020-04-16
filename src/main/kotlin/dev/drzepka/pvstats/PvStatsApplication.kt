package dev.drzepka.pvstats

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PvStatsApplication

fun main(args: Array<String>) {
	runApplication<PvStatsApplication>(*args)
}
