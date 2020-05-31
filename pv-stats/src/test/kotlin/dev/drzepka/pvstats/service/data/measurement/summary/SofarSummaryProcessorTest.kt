package dev.drzepka.pvstats.service.data.measurement.summary

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.model.InstantValue
import dev.drzepka.pvstats.service.DeviceDataService
import dev.drzepka.pvstats.service.data.summary.SofarSummaryProcessor
import dev.drzepka.pvstats.service.data.summary.Summary
import dev.drzepka.pvstats.util.kAny
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.Instant

class SofarSummaryProcessorTest {

    private val deviceDataService = mock<DeviceDataService> {
        on { getInt(kAny(), kAny(), Mockito.anyBoolean()) } doAnswer { dailyProduction }
    }

    private var dailyProduction: InstantValue<Int>? = null

    @Test
    fun `check processing with device data service`() {
        dailyProduction = InstantValue(23550, Instant.now())

        // These totalWhs should be ignored
        val previous = Summary()
        previous.totalWh = 300
        val measurement = EnergyMeasurement()
        measurement.totalWh = 400

        val service = SofarSummaryProcessor(deviceDataService)
        val current = Summary()
        service.calculateSummary(previous, current, emptyList())

        then(current.deltaWh).isEqualTo(23550)
    }
}