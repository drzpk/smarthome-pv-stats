package dev.drzepka.pvstats.service.data

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.repository.MeasurementRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList

class MeasurementServiceTest {

    private val savedMeasurements = ArrayList<EnergyMeasurement>()
    private val storedMeasurements = ArrayList<EnergyMeasurement>()

    private var findForDateRangeFrom: Date? = null
    private var findForDateRangeTo: Date? = null

    private val measurementRepository = mock<MeasurementRepository> {
        on { findForDateRange(Mockito.anyInt(), any(), any()) } doAnswer {
            findForDateRangeFrom = it.arguments[1] as Date
            findForDateRangeTo = it.arguments[2] as Date
            emptyList()
        }
        on { findLast(Mockito.anyInt()) } doReturn null
        on { saveAll(Mockito.anyCollection()) } doAnswer {
            savedMeasurements.addAll(it.getArgument(0) as List<EnergyMeasurement>)
            savedMeasurements
        }
    }

    @BeforeEach
    fun prepare() {
        savedMeasurements.clear()
        storedMeasurements.clear()
    }

    @Test
    fun `should get all measurements for given day`() {
        val service = getService()
        val now = LocalDate.now()
        service.getAllForDay(getDevice(), now)

        Assertions.assertEquals(Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), findForDateRangeFrom)
    }

    @Test
    fun `should generate a new measurement if none was found`() {
        val service = getService()
        val lastMeasurement = service.getLastMeasurement(getDevice())
        Assertions.assertEquals(0, lastMeasurement.timestamp.time)
    }

    @Test
    fun `should store all new measurements`() {
        val service = getService()
        val newItems = getMeasurements(LocalDateTime.now(), 5)
        val lastStored = getMeasurements(LocalDateTime.now().minusDays(10), 1).first()

        service.storeNewMeasurements(newItems, lastStored)
        Assertions.assertEquals(5, savedMeasurements.size)
    }

    @Test
    fun `should store some of new measurements`() {
        val service = getService()
        val newItems = getMeasurements(LocalDateTime.now(), 10)
        val lastStored = newItems[3]

        service.storeNewMeasurements(newItems, lastStored)
        Assertions.assertEquals(6, savedMeasurements.size)
    }

    @Test
    fun `should not store existing measurements`() {
        val service = getService()
        val newItems = getMeasurements(LocalDateTime.now(), 10)
        val lastStored = newItems.last()

        service.storeNewMeasurements(newItems, lastStored)
        Assertions.assertEquals(0, savedMeasurements.size)
    }

    private fun getService(): MeasurementService = MeasurementService(measurementRepository)

    private fun getDevice(): Device = Device()

    private fun getMeasurements(since: LocalDateTime, count: Int): List<EnergyMeasurement> {
        var current = since
        return (0 until count).map {
            val measurement = EnergyMeasurement()
            measurement.timestamp = Date.from(current.atZone(ZoneId.systemDefault()).toInstant())
            current = current.plusMinutes(5)
            measurement
        }.toList()
    }
}