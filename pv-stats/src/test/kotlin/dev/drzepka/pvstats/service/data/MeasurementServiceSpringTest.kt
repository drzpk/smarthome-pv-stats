package dev.drzepka.pvstats.service.data

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import dev.drzepka.pvstats.autoconfiguration.CachingAutoConfiguration
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.repository.MeasurementRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.cache.CacheManager

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [CachingAutoConfiguration::class])
class MeasurementServiceSpringTest {

    @Autowired
    private var cacheManager: CacheManager? = null

    private val measurementRepository = mock<MeasurementRepository>()

    @Test
    fun `should save distinct consecutive measurements`() {
        val first = getMeasurement(100)
        val second = getMeasurement(150)
        val third = getMeasurement(200)

        val service = getService()
        service.saveMeasurement(first)
        service.saveMeasurement(second)
        service.saveMeasurement(third)

        verify(measurementRepository, times(1)).save(eq(first))
        verify(measurementRepository, times(1)).save(eq(second))
        verify(measurementRepository, times(1)).save(eq(third))
    }

    @Test
    fun `should override second and following duplicated measurements instead of saving them`() {
        val firstUnique = getMeasurement(100)
        val secondUnique = getMeasurement(150)
        val thirdDuplicated = getMeasurement(150)
        val fourthDuplicated = getMeasurement(150)
        val fifthDuplicated = getMeasurement(150)
        val sixthUnique = getMeasurement(250)
        val seventhDuplicated = getMeasurement(250)
        val eightUnique = getMeasurement(300)
        val ninethUnique = getMeasurement(400)

        val service = getService()
        service.saveMeasurement(firstUnique)
        service.saveMeasurement(secondUnique)
        service.saveMeasurement(thirdDuplicated)
        service.saveMeasurement(fourthDuplicated)
        service.saveMeasurement(fifthDuplicated)
        service.saveMeasurement(sixthUnique)
        service.saveMeasurement(seventhDuplicated)
        service.saveMeasurement(eightUnique)
        service.saveMeasurement(ninethUnique)

        verify(measurementRepository, times(1)).save(eq(firstUnique))
        verify(measurementRepository, times(1)).save(eq(secondUnique))
        verify(measurementRepository, times(1)).save(eq(thirdDuplicated)) // first duplicate is saved and then updated, if necesssary
        verify(measurementRepository, times(0)).save(eq(fourthDuplicated))
        verify(measurementRepository, times(0)).save(eq(fifthDuplicated))
        verify(measurementRepository, times(1)).save(eq(sixthUnique))
        verify(measurementRepository, times(1)).save(eq(seventhDuplicated))
        verify(measurementRepository, times(1)).save(eq(eightUnique))
        verify(measurementRepository, times(1)).save(eq(ninethUnique))
    }

    @Test
    fun `should NOT override non-duplicates`() {
        // On some inverters only one of the three values may change
        val firstValue = getMeasurement(100, 10)
        val secondValue = getMeasurement(100, 20)
        val thirdValue = getMeasurement(100, 30)
        val fourthValue = getMeasurement(100, 30)
        val fifthValue = getMeasurement(100, 30, 10)

        val service = getService()
        service.saveMeasurement(firstValue)
        service.saveMeasurement(secondValue)
        service.saveMeasurement(thirdValue)
        service.saveMeasurement(fourthValue)
        service.saveMeasurement(fifthValue)

        verify(measurementRepository, times(1)).save(eq(firstValue))
        verify(measurementRepository, times(1)).save(eq(secondValue))
        verify(measurementRepository, times(1)).save(eq(thirdValue))
        verify(measurementRepository, times(1)).save(eq(fourthValue))
        verify(measurementRepository, times(1)).save(eq(fifthValue))
    }

    private fun getMeasurement(totalWh: Int, deltaWh: Int = 0, powerW: Int = 0): EnergyMeasurement = EnergyMeasurement().apply {
        this.deviceId = 1
        this.totalWh = totalWh
        this.deltaWh = deltaWh
        this.powerW = powerW
    }

    private fun getService(): MeasurementService = MeasurementService(measurementRepository, mock(), cacheManager!!)

}