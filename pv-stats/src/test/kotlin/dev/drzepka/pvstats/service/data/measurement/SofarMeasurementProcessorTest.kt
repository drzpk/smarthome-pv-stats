package dev.drzepka.pvstats.service.data.measurement

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import dev.drzepka.pvstats.common.model.vendor.SofarData
import dev.drzepka.pvstats.common.util.hexStringToBytes
import dev.drzepka.pvstats.config.MeasurementConfig
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.model.InstantValue
import dev.drzepka.pvstats.repository.MeasurementRepository
import dev.drzepka.pvstats.service.DeviceDataService
import dev.drzepka.pvstats.util.kAny
import dev.drzepka.pvstats.util.kEq
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.cache.CacheManager

class SofarMeasurementProcessorTest {

    private val deviceDataService = mock<DeviceDataService> {
        on { getInt(kAny(), kEq(DeviceDataService.Property.DAILY_PRODUCTION), Mockito.anyBoolean()) } doAnswer { dailyProduction }
    }
    private val cacheManager = mock<CacheManager> {}
    private val measurementRepository = mock<MeasurementRepository> {
        on { findLast(Mockito.anyInt()) } doAnswer { lastMeasurement }
        on { save(kAny()) } doAnswer { savedMeasurement = it.arguments[0] as EnergyMeasurement; savedMeasurement }
    }

    // Input
    private val measurementConfig = MeasurementConfig()
    private var dailyProduction: InstantValue<Int>? = null
    private var lastMeasurement: EnergyMeasurement? = null

    // Output
    private var savedMeasurement: EnergyMeasurement? = null

    @Test
    fun `check processing with OUTDATED DAILY_PRODUCTION device data`() {
        dailyProduction = InstantValue(23400, Instant.now().minus(Duration.ofDays(1)))
        lastMeasurement = EnergyMeasurement()
        lastMeasurement?.totalWh = 63500
        lastMeasurement?.timestamp = Date.from(Instant.now().minusSeconds(3600))

        getService().process(getDevice(), getSofarData())

        // A little hacky solution - parameters were carefully chosen to ensure that if DAILY_PRODUCTION
        // data is used, the outcome is different
        then(savedMeasurement?.totalWh).isEqualTo(64000)
    }

    @Test
    fun `check estimation - no correction`() {
        val service = getService()
        val estimation = service.getEstimatedTotalProductionWh(10400, 10000, 200)
        Assertions.assertEquals(10600, estimation)
    }

    @Test
    fun `check estimation - correction to up`() {
        val service = getService()
        val estimation = service.getEstimatedTotalProductionWh(11050, 12000, 900)
        Assertions.assertEquals(12450, estimation)
    }

    @Test
    fun `check estimation - correction to down`() {
        val service = getService()
        val estimation = service.getEstimatedTotalProductionWh(10400, 10000, 700)
        Assertions.assertEquals(10850, estimation)
    }

    @Test
    fun `check estimation - big difference`() {
        val service = getService()
        val estimation = service.getEstimatedTotalProductionWh(0, 10000, 900)
        Assertions.assertEquals(10000, estimation)
    }

    private fun getService(): SofarMeasurementProcessor = SofarMeasurementProcessor(deviceDataService, measurementRepository, measurementConfig, cacheManager)

    private fun getDevice(): Device = Device()

    private fun getSofarData(): SofarData {
        // Energy today: 23550
        // Energy total: 64000
        val bytes = hexStringToBytes("a5610010150072f3a0386602018e8002009c2400006232b4" + "5e01034e0002000000000000000000000f22027d0317000100f7000000f00041138609890158096901580953015700" +
                "0000400000002c093302800026003219e00f18031d003c000000010000054d087206cdccad0315")

        // No time to create mock class so I'm using
        return SofarData(bytes.copyOfRange(27, bytes.size))
    }
}