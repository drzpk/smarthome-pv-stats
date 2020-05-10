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

class SourceLogger(pvStatsConfig: PvStatsConfig, private val sourceConfig: SourceConfig) {

    private val log by Logger()
    private val objectMapper = ObjectMapper()

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
            log.severe("Error while collecting data for source ${sourceConfig.sourceName}: ${e.message}\n")
        }
    }

    private fun doExecute() {
        val connector = SofarConnector()
        val data = connector.getData(sourceConfig) ?: return
        sendData(data)
    }

    private fun sendData(data: VendorData) {
        val url = URL(endpointUrl)
        val connection = url.openConnection() as HttpURLConnection
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