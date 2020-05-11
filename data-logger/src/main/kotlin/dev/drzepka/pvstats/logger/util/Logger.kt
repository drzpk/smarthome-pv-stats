package dev.drzepka.pvstats.logger.util

import dev.drzepka.pvstats.logger.PVStatsDataLogger
import java.io.File
import java.time.LocalDate
import java.util.logging.FileHandler
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Logger : ReadOnlyProperty<Any, Logger> {

    override fun getValue(thisRef: Any, property: KProperty<*>): Logger {
        val logger = Logger.getLogger(thisRef.javaClass.simpleName)
        logger.addHandler(getFileHandler())
        return logger
    }

    companion object {
        private var fileHandler: FileHandler? = null

        @Synchronized
        private fun getFileHandler(): FileHandler {
            if (fileHandler != null) return fileHandler!!

            val logDirectory = File(PVStatsDataLogger.mainConfig.logDirectory)
            if (!logDirectory.isDirectory)
                throw IllegalArgumentException("Logging directory ${logDirectory.absolutePath} wasn't found")

            val now = LocalDate.now()
            val month = now.monthValue.toString().padStart(2, '0')
            val day = now.dayOfMonth.toString().padStart(2, '0')
            fileHandler = FileHandler("${logDirectory.absolutePath}/data-logger-${now.year}-$month-$day.log", true)
            fileHandler!!.formatter = SimpleFormatter()
            return fileHandler!!
        }
    }
}