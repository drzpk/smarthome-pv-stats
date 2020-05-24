package dev.drzepka.pvstats.logger.connector

import dev.drzepka.pvstats.common.model.sma.SMADashValues
import dev.drzepka.pvstats.common.model.sma.SMAMeasurement
import dev.drzepka.pvstats.common.model.vendor.SMAData
import dev.drzepka.pvstats.common.model.vendor.VendorData
import dev.drzepka.pvstats.logger.connector.base.DataType
import dev.drzepka.pvstats.logger.connector.base.HttpConnector
import dev.drzepka.pvstats.logger.model.config.SourceConfig
import java.net.URI

@Suppress("REDUNDANT_ELSE_IN_WHEN")
class SMAConnector : HttpConnector() {
    override val supportedDataTypes = listOf(DataType.METRICS, DataType.MEASUREMENT)
    // SMA's internal server doesn't use a valid certificate
    override val skipCertificateCheck = true

    override fun getUrl(config: SourceConfig, dataType: DataType): String {
        if (!config.url.startsWith("https://"))
            throw IllegalStateException("Expected https connection for SMA device")

        val suffix = when (dataType) {
            DataType.METRICS -> "/dyn/getDashValues.json"
            DataType.MEASUREMENT -> "/dyn/getDashLogger.json"
            else -> null
        }!!

        return URI.create(config.url + suffix).normalize().toString()
    }

    override fun parseResponseData(dataType: DataType, bytes: ByteArray): VendorData {
        var dashValues: SMADashValues? = null
        var measurement: SMAMeasurement? = null

        when (dataType) {
            DataType.METRICS -> dashValues = mapper.readValue(bytes, SMADashValues::class.java)
            DataType.MEASUREMENT -> measurement = mapper.readValue(bytes, SMAMeasurement::class.java)
        }

        return SMAData(measurement, dashValues)
    }
}