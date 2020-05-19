package dev.drzepka.pvstats.service.data

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.entity.EnergyMeasurementDailySummary
import dev.drzepka.pvstats.repository.EnergyMeasurementDailySummaryRepository
import dev.drzepka.pvstats.service.DeviceService
import dev.drzepka.pvstats.util.MockCache
import dev.drzepka.pvstats.util.kAny
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*
import javax.cache.CacheManager

class DailySummaryServiceTest {

    private val deviceService = mock<DeviceService> {}
    private val measurementService = mock<MeasurementService> {
        on { getFirstForDevice(kAny()) } doAnswer { firstMeasurement }
    }
    private val energyMeasurementDailySummaryRepository = mock<EnergyMeasurementDailySummaryRepository> {
        on { findFirstByDeviceOrderByCreatedAtDesc(kAny()) } doAnswer { lastSummary }
        on { save(kAny()) } doAnswer {
            val saved = it.arguments[0] as EnergyMeasurementDailySummary
            savedSummaries.add(saved)
            saved
        }
    }
    private val cacheManager = mock<CacheManager> {
        on { getCache<Any, Any>(Mockito.anyString()) } doAnswer {
            MockCache()
        }
    }

    private var firstMeasurement = EnergyMeasurement()
    private var lastSummary = EnergyMeasurementDailySummary()
    private var savedSummaries = ArrayList<EnergyMeasurementDailySummary>()

    @Test
    fun `check creating single summary`() {
        val service = getService()
        lastSummary.createdAt = LocalDate.now().minusDays(2)

        service.createMissingSummaries(getDevice())

        Assertions.assertEquals(1, savedSummaries.size)
        Assertions.assertEquals(LocalDate.now().minusDays(1), savedSummaries[0].createdAt)
    }

    @Test
    fun `check creating multiple summaries`() {
        val service = getService()
        lastSummary.createdAt = LocalDate.now().minusDays(4)

        service.createMissingSummaries(getDevice())

        Assertions.assertEquals(3, savedSummaries.size)
        Assertions.assertEquals(LocalDate.now().minusDays(3), savedSummaries[0].createdAt)
        Assertions.assertEquals(LocalDate.now().minusDays(2), savedSummaries[1].createdAt)
        Assertions.assertEquals(LocalDate.now().minusDays(1), savedSummaries[2].createdAt)
    }

    @Test
    fun `check not creating summary - missing data`() {
        val service = getService()

        service.createMissingSummaries(getDevice())

        Assertions.assertEquals(0, savedSummaries.size)
    }

    @Test
    fun `check calculation for day`() {
        val service = getService()
        val now = LocalTime.now()
        val data = listOf(
                getMeasurement(now.minusMinutes(5), 100, 100),
                getMeasurement(now, 200, 100),
                getMeasurement(now.plusMinutes(5), 300, 100)
        )

        val summary = getSummaryEntity()
        service.calculateSummary(summary, data)

        Assertions.assertEquals(300, summary.totalWh)
        Assertions.assertEquals(300, summary.deltaWh)
        Assertions.assertEquals(1200f, summary.avgPower, 0.5f)
        Assertions.assertEquals(1200, summary.maxPower)
    }

    @Test
    fun `check calculation for day - no data`() {
        val service = getService()
        val data = emptyList<EnergyMeasurement>()

        val summary = getSummaryEntity()
        service.calculateSummary(summary, data)

        Assertions.assertEquals(0, summary.totalWh)
        Assertions.assertEquals(0, summary.deltaWh)
        Assertions.assertEquals(0f, summary.avgPower)
        Assertions.assertEquals(0, summary.maxPower)
    }

    @Test
    fun `check calculation for day - single record`() {
        val service = getService()
        val data = listOf(getMeasurement(LocalTime.now(), 100, 50))

        val summary = getSummaryEntity()
        service.calculateSummary(summary, data)

        Assertions.assertEquals(100, summary.totalWh)
        Assertions.assertEquals(100, summary.deltaWh)
        Assertions.assertEquals(0f, summary.avgPower)
        Assertions.assertEquals(0, summary.maxPower)
    }

    @Test
    fun `check calculation for day - no time difference`() {
        val service = getService()
        val now = LocalTime.now()
        val data = listOf(
                getMeasurement(now, 100, 50),
                getMeasurement(now, 200, 50)
        )

        val summary = getSummaryEntity()
        service.calculateSummary(summary, data)

        Assertions.assertEquals(0f, summary.avgPower)
        Assertions.assertEquals(0, summary.maxPower)
    }

    private fun getDevice(): Device = Device()

    private fun getService(): DailySummaryService = DailySummaryService(deviceService, measurementService, energyMeasurementDailySummaryRepository, cacheManager)

    private fun getSummaryEntity(): EnergyMeasurementDailySummary {
        return EnergyMeasurementDailySummary()
    }

    private fun getMeasurement(time: LocalTime, totalWh: Int, deltaWh: Int): EnergyMeasurement {
        val measurement = EnergyMeasurement()
        measurement.timestamp = Date.from(LocalDateTime.now().with(time).atZone(ZoneId.systemDefault()).toInstant())
        measurement.totalWh = totalWh
        measurement.deltaWh = deltaWh
        return measurement
    }

}