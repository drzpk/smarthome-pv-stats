package dev.drzepka.pvstats

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableConfigurationProperties
class PvStatsApplication

fun main(args: Array<String>) {
    runApplication<PvStatsApplication>(*args)
}
