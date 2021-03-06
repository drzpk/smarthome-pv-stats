package dev.drzepka.pvstats.service.data

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.entity.EnergyMeasurementDailySummary
import dev.drzepka.pvstats.repository.EnergyMeasurementDailySummaryRepository
import dev.drzepka.pvstats.service.DeviceService
import dev.drzepka.pvstats.service.data.summary.Summary
import dev.drzepka.pvstats.service.data.summary.SummaryProcessor
import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.*

class DailySummaryServiceTest {

    private val deviceService = mock<DeviceService> {
        on { getActiveDevices() } doReturn listOf(Device())
    }
    private val measurementService = mock<MeasurementService> {
        on { getAllForDay(any(), any()) } doReturn emptyList()
        on { getFirstMeasurement(any()) } doAnswer { firstMeasurement }
    }
    private val energyMeasurementDailySummaryRepository = mock<EnergyMeasurementDailySummaryRepository> {
        on { findFirstByDeviceOrderByCreatedAtDesc(any()) } doAnswer { lastSummary }
    }
    private val handlerResolverService = mock<HandlerResolverService> {
        on { summary(any()) } doReturn MockProcessor()
    }

    // Input
    private var firstMeasurement: EnergyMeasurement? = null
    private var lastSummary: EnergyMeasurementDailySummary? = null

    // Output
    private val calculatedForDays = ArrayList<LocalDate>()

    @Test
    fun `should correctly create first summary`() {
        val service = getService()
        firstMeasurement = EnergyMeasurement()
        firstMeasurement?.timestamp = Date.from(Instant.now().minus(Duration.ofDays(3)))

        service.createSummary()

        then(calculatedForDays).hasSize(3)
        then(calculatedForDays[0]).isEqualTo(LocalDate.now().minusDays(3))
        then(calculatedForDays[1]).isEqualTo(LocalDate.now().minusDays(2))
        then(calculatedForDays[2]).isEqualTo(LocalDate.now().minusDays(1))
    }

    @Test
    fun `should correctly create a single summary for yesterday`() {
        val service = getService()
        lastSummary = EnergyMeasurementDailySummary()
        lastSummary?.createdAt = LocalDate.now().minusDays(2)

        service.createSummary()

        then(calculatedForDays).hasSize(1)
        then(calculatedForDays[0]).isEqualTo(LocalDate.now().minusDays(1))
    }

    @Test
    fun `should not create summary - missing data`() {
        val service = getService()

        service.createSummary()

        then(calculatedForDays).isEmpty()
    }

    private fun getService(): DailySummaryService = DailySummaryService(deviceService, measurementService, energyMeasurementDailySummaryRepository, handlerResolverService)

    private inner class MockProcessor : SummaryProcessor() {
        override val deviceType = DeviceType.GENERIC

        override fun calculateSummary(previous: Summary?, current: Summary, data: List<EnergyMeasurement>) {
            calculatedForDays.add(current.createdAt)
        }
    }
}