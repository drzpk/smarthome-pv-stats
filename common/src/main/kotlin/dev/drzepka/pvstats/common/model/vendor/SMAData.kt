package dev.drzepka.pvstats.common.model.vendor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dev.drzepka.pvstats.common.model.sma.SMADashValues
import dev.drzepka.pvstats.common.model.sma.SMAMeasurement
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class SMAData(
        val measurement: SMAMeasurement?,
        val dashValues: SMADashValues?
) : VendorData() {

    override fun serialize(): Any {
        val pack = DataPackage()
        pack.measurement = measurement
        pack.dashValues = dashValues
        val string = objectMapper.writeValueAsString(pack)

        val byteStream = ByteArrayOutputStream()
        val gzipStream = GZIPOutputStream(byteStream)
        gzipStream.write(string.toByteArray(StandardCharsets.UTF_8))
        gzipStream.close()

        val encoded = Base64.getEncoder().encodeToString(byteStream.toByteArray())
        byteStream.close()
        return encoded
    }

    private class DataPackage {
        var measurement: SMAMeasurement? = null
        var dashValues: SMADashValues? = null
    }

    companion object {
        private val objectMapper = ObjectMapper()

        init {
            objectMapper.registerModule(JavaTimeModule())
        }

        fun deserialize(any: Any): SMAData {
            val encoded = Base64.getDecoder().decode(any as String)
            val stream = GZIPInputStream(ByteArrayInputStream(encoded))
            val raw = stream.readBytes()
            stream.close()

            val dataPackage = objectMapper.readValue<DataPackage>(raw, DataPackage::class.java)
            return SMAData(dataPackage.measurement, dataPackage.dashValues)
        }
    }
}