package dev.drzepka.pvstats.service.data.measurement.summary

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import dev.drzepka.pvstats.common.util.hexStringToBytes
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
        on { getBytes(kAny(), kAny(), Mockito.anyBoolean()) } doAnswer { bytesValue }
    }

    // Energy today: 23550
    private val bytes = hexStringToBytes("a5610010150072f3a0386602018e8002009c2400006232b4" +
            "5e01034e0002000000000000000000000f22027d0317000100f7000000f00041138609890158096901580953015700" +
            "0000400000002c093302800026003219e00f18031d003c000000010000054d087206cdccad0315").toByteArray()

    private var bytesValue: InstantValue<ByteArray>? = null

    @Test
    fun `check processing with device data service`() {
        bytesValue = InstantValue(bytes.copyOfRange(27, bytes.size), Instant.now())

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