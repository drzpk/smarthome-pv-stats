package dev.drzepka.pvstats.service.data

import com.nhaarman.mockitokotlin2.*
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.repository.MeasurementRepository
import dev.drzepka.pvstats.util.MockCache
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.cache.CacheManager
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
        val lastMeasurement = service.getLastMeasurement(getDevice())!!
        Assertions.assertEquals(0, lastMeasurement.timestamp.time)
    }

    @Test
    fun `should store all new measurements`() {
        val service = getService()
        val newItems = getMeasurements(LocalDateTime.now(), 5)
        val lastStored = getMeasurements(LocalDateTime.now().minusDays(10), 1).first()

        service.saveMeasurements(newItems, lastStored)
        Assertions.assertEquals(5, savedMeasurements.size)
    }

    @Test
    fun `should store only measurement more recent than the previously stored one`() {
        val service = getService()
        val newItems = getMeasurements(LocalDateTime.now(), 10)
        val lastStored = newItems[3]

        service.saveMeasurements(newItems, lastStored)
        Assertions.assertEquals(6, savedMeasurements.size)
    }

    @Test
    fun `should not store existing measurements`() {
        val service = getService()
        val newItems = getMeasurements(LocalDateTime.now(), 10)
        val lastStored = newItems.last()

        service.saveMeasurements(newItems, lastStored)
        Assertions.assertEquals(0, savedMeasurements.size)
    }

    @Test
    fun `should save multiple measurements without removing duplicates (because they don't exist)`() {
        val lastStored = getMeasurement(0, 0)
        lastStored.timestamp = Date.from(Instant.now().minusSeconds(10))
        val measurements = listOf(
                getMeasurement(1, 100),
                getMeasurement(2, 200),
                getMeasurement(3, 300),
                getMeasurement(4, 400),
                getMeasurement(5, 500)
        )

        getService().saveMeasurements(measurements, lastStored)

        val captor = argumentCaptor<Iterable<EnergyMeasurement>>()
        verify(measurementRepository, times(1)).saveAll<EnergyMeasurement>(captor.capture())
        then(captor.firstValue).containsExactlyElementsOf(measurements)
    }

    @Test
    fun `should save multiple measurements and remove middle duplicates`() {
        val lastStored = getMeasurement(0, 0)
        lastStored.timestamp = Date.from(Instant.now().minusSeconds(10))
        val allMeasurements = listOf(
                getMeasurement(1, 100),
                getMeasurement(2, 200),
                getMeasurement(3, 300),
                getMeasurement(4, 300),
                getMeasurement(5, 300),
                getMeasurement(6, 300),
                getMeasurement(7, 400)
        )

        val expectedToBeSaved = listOf(
                allMeasurements[0],
                allMeasurements[1],
                allMeasurements[2],
                allMeasurements[5],
                allMeasurements[6]
        )

        testMultiple(lastStored, allMeasurements, expectedToBeSaved)
    }

    @Test
    fun `should save multiple measurements and remove middle duplicates - left edge case`() {
        val lastStored = getMeasurement(0, 100)
        lastStored.timestamp = Date.from(Instant.now().minusSeconds(10))
        val allMeasurements = listOf(
                getMeasurement(1, 100),
                getMeasurement(2, 100),
                getMeasurement(3, 200)
        )

        val expectedToBeSaved = listOf(
                allMeasurements[1],
                allMeasurements[2]
        )

        testMultiple(lastStored, allMeasurements, expectedToBeSaved)
    }

    @Test
    fun `should save multiple measurements and remove middle duplicates - right edge case`() {
        val lastStored = getMeasurement(0, 0)
        lastStored.timestamp = Date.from(Instant.now().minusSeconds(10))
        val allMeasurements = listOf(
                getMeasurement(1, 100),
                getMeasurement(2, 200),
                getMeasurement(3, 200),
                getMeasurement(4, 200),
                getMeasurement(5, 200)
        )

        val expectedToBeSaved = listOf(
                allMeasurements[0],
                allMeasurements[1],
                allMeasurements[4]
        )

        testMultiple(lastStored, allMeasurements, expectedToBeSaved)
    }

    private fun testMultiple(lastStored: EnergyMeasurement, toBeSaved: List<EnergyMeasurement>, expected: List<EnergyMeasurement>) {
        getService().saveMeasurements(toBeSaved, lastStored)

        val captor = argumentCaptor<Iterable<EnergyMeasurement>>()
        verify(measurementRepository, times(1)).saveAll<EnergyMeasurement>(captor.capture())
        then(captor.firstValue).containsExactlyElementsOf(expected)
    }

    private fun getService(): MeasurementService {
        val manager = mock<CacheManager> {
            on { getCache<Any, Any>(any()) } doReturn MockCache()
        }
        return MeasurementService(measurementRepository, mock(), manager)
    }

    private fun getDevice(): Device = Device()

    private fun getMeasurements(since: LocalDateTime, count: Int): List<EnergyMeasurement> {
        var current = since
        return (0 until count).map {
            val measurement = EnergyMeasurement()
            measurement.timestamp = Date.from(current.atZone(ZoneId.systemDefault()).toInstant())
            measurement.totalWh = it * 100
            current = current.plusMinutes(5)
            measurement
        }.toList()
    }

    private fun getMeasurement(id: Int, totalWh: Int): EnergyMeasurement = EnergyMeasurement().apply {
        this.deviceId = 1
        this.id = id
        this.totalWh = totalWh
    }
}