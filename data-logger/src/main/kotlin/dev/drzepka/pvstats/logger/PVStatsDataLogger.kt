package dev.drzepka.pvstats.logger

import dev.drzepka.pvstats.logger.model.config.PvStatsConfig
import dev.drzepka.pvstats.logger.model.config.SourceConfig
import dev.drzepka.pvstats.logger.util.Logger
import dev.drzepka.pvstats.logger.util.PropertiesLoader
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PVStatsDataLogger {

    companion object {

        const val DEBUG = false

        private val log by Logger()

        @JvmStatic
        fun main(args: Array<String>) {
            log.info("Loading configuration")

            val loader = getLoader()
            val sourceNames = SourceConfig.getAvailableNames(loader)
            if (sourceNames.isEmpty()) {
                log.info("No sources were found in configuration file, exiting")
                return
            }

            val pvStatsConfig = PvStatsConfig.loadFromProperties(loader)

            val executorService = Executors.newScheduledThreadPool(4)
            sourceNames.forEach { _ ->
                val config = SourceConfig.loadFromProperties("name", loader)
                val sourceExecutor = SourceLogger(pvStatsConfig, config)
                executorService.scheduleAtFixedRate(
                        sourceExecutor::execute,
                        getInitialDelay(sourceExecutor.getInterval()),
                        sourceExecutor.getInterval().toLong(),
                        TimeUnit.SECONDS
                )
            }

            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)
        }

        private fun getLoader(): PropertiesLoader {
            return PropertiesLoader(if (true) "default.properties" else "config.properties")
        }

        private fun getInitialDelay(intervalSeconds: Int): Long {
            val seconds = when (intervalSeconds) {
                60 -> 60 - LocalTime.now().second
                30 -> 30 - LocalTime.now().second.rem(30)
                15 -> 15 - LocalTime.now().second.rem(15)
                else -> 0
            }
            return seconds * 1000L
        }
    }
}