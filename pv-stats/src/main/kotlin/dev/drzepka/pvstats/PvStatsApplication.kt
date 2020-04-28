package dev.drzepka.pvstats

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableConfigurationProperties
@EnableTransactionManagement
class PvStatsApplication

fun main(args: Array<String>) {
    runApplication<PvStatsApplication>(*args)
}
