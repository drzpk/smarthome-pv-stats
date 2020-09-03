package dev.drzepka.pvstats.service

import dev.drzepka.pvstats.autoconfiguration.CachingAutoConfiguration
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.DeviceData
import dev.drzepka.pvstats.model.InstantValue
import dev.drzepka.pvstats.repository.DeviceDataRepository
import dev.drzepka.pvstats.util.Logger
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import javax.cache.CacheManager
import javax.transaction.Transactional

// todo: unused variables removal mechanism
@Service
class DeviceDataService(
        private val deviceDataRepository: DeviceDataRepository,
        cacheManager: CacheManager
) {
    private val log by Logger()

    private val cache = cacheManager.getCache<Any, Any>(CachingAutoConfiguration.CACHE_DEVICE_DATA)

    fun getInt(device: Device, property: Property, invalidate: Boolean = false): InstantValue<Int>? {
        val str = getString(device, property, invalidate)
        return if (str != null) InstantValue(str.value.toInt(), str.instant) else null
    }

    fun getBytes(device: Device, property: Property, invalidate: Boolean = false): InstantValue<ByteArray>? {
        val str = getString(device, property, invalidate)
        return if (str != null) InstantValue(Base64.getDecoder().decode(str.value), str.instant) else null
    }

    @Suppress("UNCHECKED_CAST")
    fun getString(device: Device, property: Property, invalidate: Boolean = false): InstantValue<String>? {
        val cacheKey = getCacheKey(device, property)
        var value = cache.get(cacheKey) as InstantValue<String>?

        if (value == null) {
            val entity = deviceDataRepository.findByDeviceIdAndProperty(device.id, property.name)
            value = if (entity != null) InstantValue(entity.value, entity.updatedAt.toInstant()) else null
            if (invalidate && entity != null)
                deviceDataRepository.delete(entity)
            if (!invalidate && value != null)
                cache.put(cacheKey, value)
        } else if (invalidate) {
            cache.remove(cacheKey)
        }

        return value
    }

    @Transactional(value = Transactional.TxType.REQUIRED)
    fun set(device: Device, property: Property, value: Int) = set(device, property, value.toString())

    @Transactional(value = Transactional.TxType.REQUIRED)
    fun set(device: Device, property: Property, value: ByteArray) = set(device, property, Base64.getEncoder().encodeToString(value))

    @Transactional(value = Transactional.TxType.REQUIRED)
    fun set(device: Device, property: Property, value: String) {
        val key = getCacheKey(device, property)

        var entity = deviceDataRepository.findByDeviceIdAndProperty(device.id, property.name)
        val wasNull = entity == null
        if (entity == null) {
            log.debug("Device property ${property.name} for device $device didn't exist, creating")
            entity = DeviceData().apply {
                this.device = device
                this.property = property.name
            }
        }

        entity.value = value
        if (wasNull) {
            // If wasn't, it will be saved automatically (Spring transaction management)
            deviceDataRepository.save(entity)
        }

        cache.put(key, InstantValue(value, Instant.now()))
    }

    private fun getCacheKey(device: Device, property: Property): String = device.id.toString() + "_" + property.name

    enum class Property {
        DAILY_PRODUCTION,
        POWER,
        VENDOR_DATA
    }
}