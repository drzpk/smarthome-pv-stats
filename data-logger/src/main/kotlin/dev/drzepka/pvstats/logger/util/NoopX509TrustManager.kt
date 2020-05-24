package dev.drzepka.pvstats.logger.util

import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Allows to skip certificate verification for some hosts
 */
class NoopX509TrustManager : X509TrustManager {
    override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) = Unit
    override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) = Unit
    override fun getAcceptedIssuers(): Array<X509Certificate>? = null

    companion object {
        val sslContext: SSLContext

        init {
            val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            factory.init(null as KeyStore?)

            val context = SSLContext.getInstance("SSL")
            context.init(null, arrayOf(NoopX509TrustManager()), null)

            sslContext = context
        }

    }
}

