package dev.drzepka.pvstats.service.data

import com.nhaarman.mockitokotlin2.mock
import dev.drzepka.pvstats.repository.MeasurementRepository
import dev.drzepka.pvstats.service.DeviceDataService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.cache.CacheManager

class SofarDataProcessorServiceTest {

    private val deviceCacheService = mock<DeviceDataService> {}
    private val cacheManager = mock<CacheManager> {}
    private val measurementRepository = mock<MeasurementRepository> {}

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

    private fun getService(): SofarDataProcessorService = SofarDataProcessorService(deviceCacheService, cacheManager, measurementRepository)
}