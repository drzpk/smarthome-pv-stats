package dev.drzepka.pvstats.logger.connector

import dev.drzepka.pvstats.common.model.vendor.SofarData
import dev.drzepka.pvstats.common.model.vendor.VendorData
import dev.drzepka.pvstats.logger.PVStatsDataLogger
import dev.drzepka.pvstats.logger.model.config.SourceConfig
import dev.drzepka.pvstats.logger.util.Logger
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

abstract class Connector {
    abstract val type: Type

    protected val log by Logger()

    fun getData(config: SourceConfig): VendorData? {
        return when (type) {
            Type.SOCKET -> getSocketData(config)
        }
    }

    internal open fun getSocketRequestData(config: SourceConfig): Array<Byte> = throw NotImplementedError("socket request data isn't implemented")

    internal open fun parseSocketResponseData(config: SourceConfig, response: Array<Byte>): VendorData = throw NotImplementedError("socket response parser isn't implemented")

    private fun getSocketData(config: SourceConfig): VendorData? {
        if (PVStatsDataLogger.DEBUG) return getTestVendorData()

        val split = splitSocketUrl(config.url)
        val socket = Socket()

        try {
            socket.connect(InetSocketAddress(split.first, split.second), config.timeout * 1000)
        } catch (e: SocketTimeoutException) {
            log.warning("Connection to source ${config.sourceName} timed out (${config.url})")
        }

        socket.getOutputStream().write(getSocketRequestData(config).toByteArray())
        val inputStream = socket.getInputStream()
        while (inputStream.available() == 0) Thread.sleep(100L)
        val buffer = ByteArray(inputStream.available())
        inputStream.read(buffer)
        inputStream.close()
        socket.close()

        if (buffer.size != 110) {
            log.warning("Response from source ${config.sourceName} does not appear to contain inverter data. " +
                    "Did you supplied correct SN?")
            return null
        }

        val byteArray = buffer.toTypedArray()
        return parseSocketResponseData(config, byteArray)
    }

    private fun splitSocketUrl(url: String): Pair<String, Int> {
        val split = url.split(":")
        if (split.size != 2) throw IllegalArgumentException("Malformed url")
        return Pair(split[0], split[1].toInt())
    }

    private fun getTestVendorData(): VendorData {
        val bytes = hexStringToBytes("a5610010150072f3a0386602018e8002009c2400006232b4" +
                "5e01034e0002000000000000000000000f22027d0317000100f7000000f00041138609890158096901580953015700" +
                "0000400000002c093302800026003219e00f18031d003c000000010000054d087206cdccad0315")

        return SofarData(bytes.copyOfRange(27, bytes.size))
    }

    enum class Type {
        SOCKET
    }

    companion object {
        private const val HEX_CHARS = "0123456789abcdef"

        @JvmStatic
        fun hexStringToBytes(str: String): Array<Byte> {
            val bytes = Array<Byte>(str.length / 2) { 0 }
            for (i in str.indices step 2) {
                val upper = HEX_CHARS.indexOf(str[i].toLowerCase())
                val lower = HEX_CHARS.indexOf(str[i + 1].toLowerCase())
                bytes[i.shr(1)] = upper.shl(4).or(lower).toByte()
            }

            return bytes
        }
    }
}