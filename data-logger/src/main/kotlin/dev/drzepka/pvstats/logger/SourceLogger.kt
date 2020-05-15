package dev.drzepka.pvstats.logger

import com.fasterxml.jackson.databind.ObjectMapper
import dev.drzepka.pvstats.common.model.PutDataRequest
import dev.drzepka.pvstats.common.model.vendor.VendorData
import dev.drzepka.pvstats.logger.connector.SofarConnector
import dev.drzepka.pvstats.logger.model.config.PvStatsConfig
import dev.drzepka.pvstats.logger.model.config.SourceConfig
import dev.drzepka.pvstats.logger.util.Logger
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.logging.Level
import kotlin.math.ceil
import kotlin.system.exitProcess

class SourceLogger(private val pvStatsConfig: PvStatsConfig, private val sourceConfig: SourceConfig) {

    private val log by Logger()
    private val objectMapper = ObjectMapper()

    private val sofarConnector = SofarConnector()
    private var connectorErrorCount = 0
    private var connectorThrottling = 0
    private var throttlingCountdown = 0

    private val endpointUrl: String = pvStatsConfig.url.toString() + "/api/data"
    private val authorizationHeader: String

    init {
        val authData = pvStatsConfig.user + ":" + pvStatsConfig.password
        authorizationHeader = "Basic " + Base64.getEncoder().encodeToString(authData.toByteArray())
    }

    fun getInterval() = sourceConfig.interval

    fun execute() {
        try {
            doExecute()
        } catch (e: Exception) {
            log.log(Level.SEVERE, "Unexpected exception caught during execution of logger for source {${sourceConfig.sourceName}", e)
        } catch (t: Throwable) {
            log.log(Level.SEVERE, "Unrecoverable exception caught", t)
            exitProcess(1)
        }
    }

    private fun doExecute() {
        if (throttlingCountdown > 0) {
            throttlingCountdown--
            return
        }
        if (throttlingCountdown == 0 && connectorThrottling > 0)
            throttlingCountdown = connectorThrottling

        val dataSent = try {
            sendData()
        } catch (e: Exception) {
            if (connectorThrottling == 0)
                log.log(Level.SEVERE, "Error while collecting data for source ${sourceConfig.sourceName}", e)
            false
        }

        if (!dataSent) {
            connectorErrorCount++
            if (connectorErrorCount == 3) {
                log.warning("Inverter responded with error 3 times in a row, increasing request interval")
                log.info("Subsequent inverter connection errors won't be logged")
                calculateThrottling()
            }
        } else {
            if (connectorErrorCount > 2) {
                log.info("Inverter responded normally after $connectorErrorCount errors, restoring normal request interval")
                connectorThrottling = 0
                throttlingCountdown = 0
            }

            connectorErrorCount = 0
        }
    }

    private fun sendData(): Boolean {
        val data = sofarConnector.getData(sourceConfig, connectorThrottling > 0) ?: return false
        sendData(data)
        return true
    }

    private fun calculateThrottling() {
        connectorThrottling = ceil(120f / getInterval()).toInt()
        throttlingCountdown = connectorThrottling
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
        request.data = Base64.getEncoder().encodeToString(data.raw.toByteArray())
        return objectMapper.writeValueAsString(request)
    }
}