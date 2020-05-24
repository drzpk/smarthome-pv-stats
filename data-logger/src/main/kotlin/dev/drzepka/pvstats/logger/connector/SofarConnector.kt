package dev.drzepka.pvstats.logger.connector

import dev.drzepka.pvstats.common.model.vendor.SofarData
import dev.drzepka.pvstats.common.model.vendor.VendorData
import dev.drzepka.pvstats.logger.connector.base.DataType
import dev.drzepka.pvstats.logger.connector.base.SocketConnector
import dev.drzepka.pvstats.logger.model.config.SourceConfig

/**
 * Request message source: https://github.com/mcikosos/Inverter-Data-Logger
 */
class SofarConnector : SocketConnector() {
    override val supportedDataTypes = listOf(DataType.METRICS)

    override fun getSocketRequestData(config: SourceConfig): Array<Byte> {
        val snLittleEndian = hexStringToBytes(
                config.sn!!.toString(16).padStart(8, '0').chunked(2).reversed().joinToString(separator = ""))
        val bytes = ArrayList<Byte>(36)
        bytes.addAll(HEADER)
        bytes.addAll(snLittleEndian)
        bytes.addAll(DATE_FIELD_PREFIX)
        bytes.addAll(COMMAND)
        bytes.addAll(DEF_CHK)
        bytes.addAll(END_CODE)

        val checksum = bytes.subList(1, bytes.size - 2).sum()
        bytes[bytes.size - 2] = checksum.and(255).toByte()

        return bytes.toTypedArray()
    }

    override fun parseSocketResponseData(config: SourceConfig, response: Array<Byte>): VendorData {
        return SofarData(response.copyOfRange(27, response.size))
    }

    override fun getUrl(config: SourceConfig, dataType: DataType): String = config.url

    companion object {
        private val HEADER = hexStringToBytes("a5170010450000")
        private val DATE_FIELD_PREFIX = hexStringToBytes("020000000000000000000000000000")
        private val COMMAND = hexStringToBytes("01030000002705d0")
        private val DEF_CHK = hexStringToBytes("d8")
        private val END_CODE = hexStringToBytes("15")
    }
}