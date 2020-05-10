package dev.drzepka.pvstats.logger.util

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

            val now = LocalDate.now()
            fileHandler = FileHandler("logs/data-logger-${now.year}-${now.month}-${now.dayOfMonth}.log")
            fileHandler!!.formatter = SimpleFormatter()
            return fileHandler!!
        }
    }
}