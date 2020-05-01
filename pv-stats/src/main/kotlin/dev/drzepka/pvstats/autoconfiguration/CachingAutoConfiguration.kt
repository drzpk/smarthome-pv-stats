package dev.drzepka.pvstats.autoconfiguration

import dev.drzepka.pvstats.model.DataSourceUserDetails
import org.ehcache.config.CacheConfiguration
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ExpiryPolicyBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.core.config.DefaultConfiguration
import org.ehcache.jsr107.EhcacheCachingProvider
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.cache.jcache.JCacheCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Duration
import javax.cache.CacheManager
import javax.cache.Caching

@Configuration
@EnableCaching
class CachingAutoConfiguration {

    @Bean
    fun cacheManager(): JCacheCacheManager {
        return JCacheCacheManager(ehCacheCacheManager())
    }

    @Bean(KEY_GENERATOR_DEVICE_ID)
    fun dataSourceIdKeyGenerator(): KeyGenerator {
        return KeyGenerator { _, _, _ ->
            val principal = SecurityContextHolder.getContext().authentication.principal
            if (principal !is DataSourceUserDetails)
                "blank"
            else
                principal.dataSource.id
        }
    }

    private fun ehCacheCacheManager(): CacheManager {
        val provider = ehCacheCachingProvider()

        val defaultConfiguration = DefaultConfiguration(getCaches(), CachingAutoConfiguration::class.java.classLoader)
        return provider.getCacheManager(provider.defaultURI, defaultConfiguration)
    }

    private fun getCaches(): Map<String, CacheConfiguration<Any, Any>> {
        val smaCurrentStatsConfig = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Any::class.java, Any::class.java, ResourcePoolsBuilder.heap(10))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(6)))
                .build()

        return mapOf(Pair(CACHE_SMA_CURRENT_STATS, smaCurrentStatsConfig))
    }

    private fun ehCacheCachingProvider(): EhcacheCachingProvider = Caching.getCachingProvider() as EhcacheCachingProvider

    companion object {
        const val CACHE_SMA_CURRENT_STATS = "smaCurrentStats"
        const val KEY_GENERATOR_DEVICE_ID = "deviceIdKeyGenerator"
    }
}