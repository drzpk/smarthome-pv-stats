package dev.drzepka.pvstats.logger.connector.base

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import dev.drzepka.pvstats.common.model.vendor.VendorData
import dev.drzepka.pvstats.logger.model.config.SourceConfig
import dev.drzepka.pvstats.logger.util.NoopX509TrustManager
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.impl.client.HttpClients
import java.net.URI


abstract class HttpConnector : Connector {
    protected open val skipCertificateCheck = false

    private lateinit var httpClient: HttpClient
    private lateinit var requestConfig: RequestConfig

    override fun initialize(config: SourceConfig) {
        httpClient = if (skipCertificateCheck) certificateIgnoringHttpClient else standardHttpClient

        requestConfig = RequestConfig.custom()
                .setConnectTimeout(config.timeout * 1000)
                .setConnectionRequestTimeout(config.timeout * 1000)
                .setSocketTimeout(config.timeout * 1000)
                .build()
    }

    final override fun getData(config: SourceConfig, dataType: DataType, silent: Boolean): VendorData? {
        val uri = URI(getUrl(config, dataType))

        val get = HttpGet(uri)
        get.config = requestConfig

        val bytes = httpClient.execute(get) {
            val stream = it.entity.content
            val bytes = stream.readBytes()
            stream.close()
            bytes
        }

        return parseResponseData(dataType, bytes)
    }

    abstract fun parseResponseData(dataType: DataType, bytes: ByteArray): VendorData

    companion object {
        @JvmStatic
        val mapper = ObjectMapper()

        private val standardHttpClient = HttpClients.createDefault()
        private val certificateIgnoringHttpClient = HttpClients.custom()
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setSSLContext(NoopX509TrustManager.sslContext)
                .build()

        init {
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
}