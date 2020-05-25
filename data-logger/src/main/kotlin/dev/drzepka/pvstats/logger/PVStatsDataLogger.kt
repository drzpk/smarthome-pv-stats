package dev.drzepka.pvstats.logger

import dev.drzepka.pvstats.logger.model.config.MainConfig
import dev.drzepka.pvstats.logger.model.config.PvStatsConfig
import dev.drzepka.pvstats.logger.model.config.SourceConfig
import dev.drzepka.pvstats.logger.util.Logger
import dev.drzepka.pvstats.logger.util.PropertiesLoader
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import kotlin.system.exitProcess

class PVStatsDataLogger {

    companion object {
        const val DEBUG = false

        private val loader = PropertiesLoader()
        private val log by Logger()

        val mainConfig = MainConfig.loadFromProperties(loader)

        @JvmStatic
        fun main(args: Array<String>) {
            log.info("Loading configuration")
            archiveLogs()

            val sourceLoggers = getSourceLoggers()
            if (sourceLoggers.isEmpty()) {
                log.info("No sources were found in configuration file, exiting")
                return
            }

            val executorService = Executors.newScheduledThreadPool(4)
            val initialDelay = LocalTime.MAX.toSecondOfDay() - LocalTime.now().toSecondOfDay() + 60
            executorService.scheduleAtFixedRate(this::archiveLogs, initialDelay.toLong(), 24 * 60 * 60, TimeUnit.SECONDS)

            sourceLoggers.forEach { logger ->
                logger.getIntervals().forEach { interval ->
                    executorService.scheduleAtFixedRate(
                            { logger.execute(interval.key) },
                            getInitialDelay(interval.value),
                            interval.value.toLong(),
                            TimeUnit.SECONDS
                    )
                }
            }

            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)
        }

        private fun getSourceLoggers(): List<SourceLogger> {
            val pvStatsConfig = PvStatsConfig.loadFromProperties(loader)
            val sourceNames = SourceConfig.getAvailableNames(loader)

            return sourceNames.map {
                val config = SourceConfig.loadFromProperties(it, loader)
                try {
                    SourceLogger(pvStatsConfig, config)
                } catch (e: Exception) {
                    log.log(Level.SEVERE, "Error while initializing logger ${config.name}", e)
                    exitProcess(1)
                }
            }
        }

        private fun getInitialDelay(intervalSeconds: Int): Long {
            val seconds = if (intervalSeconds.rem(5) == 0)
                intervalSeconds - LocalTime.now().second.rem(5)
            else
                0
            return seconds.toLong()
        }

        private fun archiveLogs() {
            try {
                Logger.archiveLogs()
            } catch (t: Throwable) {
                log.log(Level.SEVERE, "Unrecoverable error occurred while archiving logs", t)
                exitProcess(1)
            }
        }
    }
}