package dev.drzepka.pvstats.logger.connector

import dev.drzepka.pvstats.common.model.vendor.SMAData
import dev.drzepka.pvstats.logger.PVStatsDataLogger
import dev.drzepka.pvstats.logger.connector.base.DataType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class SMAConnectorTest {

    @Test
    fun `verify data format`() {
        val realData = getBytes(MEASUREMENT_FILENAME)

        val connector = SMAConnector()
        val data = connector.parseResponseData(DataType.MEASUREMENT, realData) as SMAData
        Assertions.assertNotEquals(0, data.measurement?.getEntries())

        // 15 April 2020 18:20:00
        val expectedFirstRecordTime = LocalDateTime.of(2020, 4, 15, 18, 20, 0).atZone(ZoneOffset.UTC).toInstant()
        val actualFirstRecordTime = data.measurement?.getEntries()?.first()?.t?.toInstant()
        Assertions.assertEquals(expectedFirstRecordTime.epochSecond, actualFirstRecordTime?.epochSecond)
    }

    @Test
    fun `check getting power`() {
        val connector = SMAConnector()
        val data = connector.parseResponseData(DataType.METRICS, getBytes(DASH_VALUES_FILENAME)) as SMAData
        Assertions.assertEquals(1639, data.dashValues?.getPower())
    }

    @Test
    fun `check getting null power`() {
        val connector = SMAConnector()
        val data = connector.parseResponseData(DataType.METRICS, getBytes(DASH_VALUES_NULL_POWER_FILENAME)) as SMAData
        Assertions.assertEquals(0, data.dashValues?.getPower())
    }

    @Test
    fun `check getting device name`() {
        val connector = SMAConnector()
        val data = connector.parseResponseData(DataType.METRICS, getBytes(DASH_VALUES_FILENAME)) as SMAData
        Assertions.assertEquals("STP4.0-3AV-40 752", data.dashValues?.getDeviceName())
    }

    private fun getBytes(filename: String): ByteArray {
        val stream = PVStatsDataLogger::class.java.classLoader.getResourceAsStream(filename)!!
        val bytes = stream.readBytes()
        stream.close()
        return bytes
    }

    companion object {
        private const val MEASUREMENT_FILENAME = "sma_measurement_data.json"
        private const val DASH_VALUES_FILENAME = "sma_dash_values.json"
        private const val DASH_VALUES_NULL_POWER_FILENAME = "sma_dash_values_null_power.json"
    }
}