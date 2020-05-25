package dev.drzepka.pvstats.logger

import com.fasterxml.jackson.databind.ObjectMapper
import dev.drzepka.pvstats.common.model.PutDataRequest
import dev.drzepka.pvstats.common.model.vendor.DeviceType
import dev.drzepka.pvstats.common.model.vendor.VendorData
import dev.drzepka.pvstats.logger.connector.SMAConnector
import dev.drzepka.pvstats.logger.connector.SofarConnector
import dev.drzepka.pvstats.logger.connector.base.Connector
import dev.drzepka.pvstats.logger.connector.base.DataType
import dev.drzepka.pvstats.logger.model.config.PvStatsConfig
import dev.drzepka.pvstats.logger.model.config.SourceConfig
import dev.drzepka.pvstats.logger.util.Logger
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.logging.Level
import kotlin.math.floor
import kotlin.system.exitProcess

class SourceLogger(private val pvStatsConfig: PvStatsConfig, private val sourceConfig: SourceConfig) {

    private val log by Logger()
    private val objectMapper = ObjectMapper()

    private val connector = getConnector(sourceConfig)
    private val connectorThrottling: Int

    private var throttle = false
    private var connectorErrorCount = 0
    private var throttlingCountdown = 0

    private val endpointUrl: String = pvStatsConfig.url.toString() + "/api/data"
    private val authorizationHeader: String

    init {
        val authData = sourceConfig.user + ":" + sourceConfig.password
        authorizationHeader = "Basic " + Base64.getEncoder().encodeToString(authData.toByteArray())

        val throttledInterval = 2 * 60 // seconds
        // Consecutive error responses will cause connector to throttle fetch request frequency.
        // Throttling is meant to be used for requests with high frequency (more frequent than throttledInterval),
        // those with lower frequency won't be throttled at all
        val minInterval = getIntervals().map { it.value }.min()!!
        connectorThrottling = floor(throttledInterval.toFloat() / minInterval).toInt()
    }

    fun getIntervals(): Map<DataType, Int> = connector.supportedDataTypes.map {
        val interval = when (it) {
            DataType.METRICS -> sourceConfig.metricsInterval
            DataType.MEASUREMENT -> sourceConfig.measurementInterval
        } ?: throw IllegalArgumentException("interval of type $it" +
                " is required by device ${sourceConfig.type} but is missing")

        Pair(it, interval)
    }.toMap()

    fun execute(dataType: DataType) {
        try {
            doExecute(dataType)
        } catch (e: Exception) {
            log.log(Level.SEVERE, "Unexpected exception caught during execution of logger for source {${sourceConfig.name}", e)
        } catch (t: Throwable) {
            log.log(Level.SEVERE, "Unrecoverable exception caught", t)
            exitProcess(1)
        }
    }

    private fun getConnector(sourceConfig: SourceConfig): Connector {
        val connector = when (sourceConfig.type) {
            DeviceType.SMA -> SMAConnector()
            DeviceType.SOFAR -> SofarConnector()
            DeviceType.GENERIC -> throw IllegalStateException("Generic device type is not meant to be used as a source")
        }

        connector.initialize(sourceConfig)
        return connector
    }

    private fun doExecute(dataType: DataType) {
        if (throttlingCountdown > 0) {
            throttlingCountdown--
            return
        }
        if (throttlingCountdown == 0 && throttle)
            throttlingCountdown = connectorThrottling

        val dataSent = try {
            sendData(dataType)
        } catch (e: Exception) {
            if (!throttle)
                log.log(Level.SEVERE, "Error while collecting data for source ${sourceConfig.name}", e)
            false
        }

        if (!dataSent) {
            connectorErrorCount++
            if (connectorErrorCount == 3) {
                log.warning("Inverter responded with error 3 times in a row, increasing request interval")
                log.info("Subsequent inverter connection errors won't be logged")
                throttle = true
                throttlingCountdown = connectorThrottling
            }
        } else {
            if (connectorErrorCount > 2) {
                log.info("Inverter responded normally after $connectorErrorCount errors, restoring normal request interval")
                throttle = false
                throttlingCountdown = 0
            }

            connectorErrorCount = 0
        }
    }

    private fun sendData(dataType: DataType): Boolean {
        val data = connector.getData(sourceConfig, dataType, throttlingCountdown > 0) ?: return false
        sendData(data)
        return true
    }

    private fun sendData(data: VendorData) {
        val url = URL(endpointUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = pvStatsConfig.timeout * 1000
        connection.readTimeout = pvStatsConfig.timeout * 1000
        connection.setRequestProperty("Authorization", authorizationHeader)
        connection.requestMethod = "PUT"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")

        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(prepareRequest(data))
        writer.close()

        if (connection.responseCode != 201)
            log.warning("Data sent failed: server returned with HTTP code ${connection.responseCode}")
    }

    private fun prepareRequest(data: VendorData): String {
        val request = PutDataRequest()
        request.type = sourceConfig.type
        request.data = data.serialize()
        return objectMapper.writeValueAsString(request)
    }
}