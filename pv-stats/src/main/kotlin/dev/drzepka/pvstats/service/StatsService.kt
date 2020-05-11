package dev.drzepka.pvstats.service

import dev.drzepka.pvstats.autoconfiguration.CachingAutoConfiguration
import dev.drzepka.pvstats.common.model.vendor.SofarData
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.model.CurrentStats
import dev.drzepka.pvstats.model.DataSourceUserDetails
import dev.drzepka.pvstats.model.DeviceType
import dev.drzepka.pvstats.service.data.DailySummaryService
import dev.drzepka.pvstats.service.data.MeasurementService
import dev.drzepka.pvstats.util.Logger
import dev.drzepka.pvstats.web.client.sma.SMAApiClient
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.net.URI
import javax.cache.CacheManager

@Service
class StatsService(
        private val smaApiClient: SMAApiClient,
        private val measurementService: MeasurementService,
        private val dailySummaryService: DailySummaryService,
        cacheManager: CacheManager
) {

    private val vendorDataCache = cacheManager.getCache<Any, Any>(CachingAutoConfiguration.CACHE_LAST_VENDOR_DATA)

    private val log by Logger()

    // todo: delete this cache after moving SMA data gathering into data-logger
    @Cacheable(value = [CachingAutoConfiguration.CACHE_SMA_CURRENT_STATS], keyGenerator = CachingAutoConfiguration.KEY_GENERATOR_DEVICE_ID)
    fun getCurrentStats(): CurrentStats? {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as DataSourceUserDetails
        val device = userDetails.dataSource.device!!
        return when (device.type) {
            DeviceType.SMA -> getSMAStats(device)
            DeviceType.SOFAR -> getSofarStats(device)
            DeviceType.UNKNOWN -> null
        }
    }

    private fun getSMAStats(device: Device): CurrentStats {
        // todo: "private" api usage, refactor this statement
        val lastMeasurement = measurementService.getLastMeasurement(device)
        val yesterdaySummary = dailySummaryService.getLastSummaryFor(device)
        val todayGeneration = if (yesterdaySummary != null) lastMeasurement.totalWh - yesterdaySummary.totalWh else 0

        val dashValues = smaApiClient.getDashValues(URI.create(device.apiUrl))
        return CurrentStats(dashValues.getPower(), dashValues.getDeviceName(), todayGeneration)
    }

    private fun getSofarStats(device: Device): CurrentStats {
        val sofarData = vendorDataCache[device.id] as SofarData?
        if (sofarData == null) {
            log.debug("No vendor data for device $device")
            return CurrentStats(0, device.name, 0)
        }

        return CurrentStats(sofarData.currentPower, "", sofarData.energyToday, sofarData.pv1Voltage, sofarData.pv1Current)
    }
}