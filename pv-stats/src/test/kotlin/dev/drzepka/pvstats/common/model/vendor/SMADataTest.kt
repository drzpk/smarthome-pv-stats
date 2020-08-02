package dev.drzepka.pvstats.common.model.vendor

import dev.drzepka.pvstats.common.model.sma.Entry
import dev.drzepka.pvstats.common.model.sma.SMADeviceData
import dev.drzepka.pvstats.common.model.sma.SMAMeasurement
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoField
import java.util.*

class SMADataTest {

    @Test
    fun `should serialize date`() {
        // SMA measurement are only accurate up to seconds
        val date = Date.from(Instant.now()
                .with(ChronoField.MILLI_OF_SECOND, 0)
                .with(ChronoField.MICRO_OF_SECOND, 0))

        val entries = listOf(Entry(date, 100))
        val deviceData = SMADeviceData()
        deviceData.currentData["1"] = entries

        val measurement = SMAMeasurement()
        measurement.result["1"] = deviceData

        val data = SMAData(measurement, null)
        val any = data.serialize()
        val restored = SMAData.deserialize(any)

        then(restored.measurement?.getEntries()?.first()?.t).isEqualTo(date)
    }
}