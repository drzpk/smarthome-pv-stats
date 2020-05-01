package dev.drzepka.pvstats.service

import dev.drzepka.pvstats.autoconfiguration.CachingAutoConfiguration
import dev.drzepka.pvstats.model.CurrentStatsResponse
import dev.drzepka.pvstats.model.DataSourceUserDetails
import dev.drzepka.pvstats.model.DeviceType
import dev.drzepka.pvstats.web.client.sma.SMAApiClient
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.net.URI

@Service
class StatsService(private val smaApiClient: SMAApiClient) {

    @Cacheable(value = [CachingAutoConfiguration.CACHE_SMA_CURRENT_STATS], keyGenerator = CachingAutoConfiguration.KEY_GENERATOR_DEVICE_ID)
    fun getCurrentStats(): CurrentStatsResponse? {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as DataSourceUserDetails
        val device = userDetails.dataSource.device!!
        return when (device.type) {
            DeviceType.SMA -> getSMAStats(device.apiUrl)
            DeviceType.UNKNOWN -> null
        }
    }

    private fun getSMAStats(apiUrl: String): CurrentStatsResponse {
        val dashValues = smaApiClient.getDashValues(URI.create(apiUrl))
        return CurrentStatsResponse(dashValues.getPower(), dashValues.getDeviceName())
    }
}