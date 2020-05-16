package dev.drzepka.pvstats.logger.util

import dev.drzepka.pvstats.logger.PVStatsDataLogger
import java.io.File
import java.time.Duration
import java.time.LocalDate
import java.util.logging.FileHandler
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import java.util.zip.GZIPOutputStream
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Logger : ReadOnlyProperty<Any, Logger> {

    override fun getValue(thisRef: Any, property: KProperty<*>): Logger {
        initializeFileHandler()
        return Logger.getLogger(thisRef.javaClass.simpleName)
    }

    companion object {
        private val archiveLogKeepTime = Duration.ofDays(7)
        private var fileHandler: FileHandler? = null

        private val logFileRegex = Regex("^data-logger-(\\d{4})-(\\d{2})-(\\d{2})\\.log$")
        private val archiveLogFileRegex = Regex("^data-logger-(\\d{4})-(\\d{2})-(\\d{2})\\.log.gz$")

        fun archiveLogs() {
            createNewLogFile()

            val today = LocalDate.now()
            val logDir = File(PVStatsDataLogger.mainConfig.logDirectory)

            logDir.listFiles()!!.forEach {
                if (!it.isFile)
                    return@forEach

                val match = logFileRegex.matchEntire(it.name) ?: return@forEach
                val logDate = getDateFromMatch(match)
                if (!logDate.isBefore(today))
                    return@forEach

                val archivedFile = File(it.parentFile, it.name + ".gz")
                val gzipOutputStream = GZIPOutputStream(archivedFile.outputStream())
                val logInputStream = it.inputStream()

                val buffer = ByteArray(1024)
                var read: Int
                do {
                    read = logInputStream.read(buffer)
                    if (read < 1)
                        continue
                    gzipOutputStream.write(buffer, 0, read)
                } while (read > 0)

                logInputStream.close()
                gzipOutputStream.close()

                it.delete()
            }

            deleteOldLogs()
        }

        private fun createNewLogFile() {
            if (fileHandler == null)
                return

            val rootLogger = Logger.getLogger("")
            rootLogger.removeHandler(fileHandler!!)
            fileHandler!!.close()
            fileHandler = null
            initializeFileHandler()
        }

        private fun deleteOldLogs() {
            val logDir = File(PVStatsDataLogger.mainConfig.logDirectory)
            logDir.listFiles()!!.forEach {
                if (!it.isFile)
                    return@forEach

                val match = archiveLogFileRegex.matchEntire(it.name) ?: return@forEach
                val logDate = getDateFromMatch(match)
                if (!logDate.isBefore(LocalDate.now().minusDays(archiveLogKeepTime.toDays())))
                    return@forEach

                it.delete()
            }

        }

        private fun getDateFromMatch(match: MatchResult): LocalDate = LocalDate.of(match.groupValues[1].toInt(), match.groupValues[2].toInt(), match.groupValues[3].toInt())

        @Synchronized
        private fun initializeFileHandler() {
            if (fileHandler != null)
                return

            fileHandler = getFileHandler()
            val rootLogger = Logger.getLogger("")
            if (rootLogger.handlers.contains(fileHandler).not())
                rootLogger.addHandler(getFileHandler())
        }

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