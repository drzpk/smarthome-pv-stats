package dev.drzepka.pvstats.service

import dev.drzepka.pvstats.autoconfiguration.CachingAutoConfiguration
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.DeviceData
import dev.drzepka.pvstats.repository.DeviceDataRepository
import dev.drzepka.pvstats.util.Logger
import org.springframework.stereotype.Service
import java.util.*
import javax.cache.CacheManager
import javax.transaction.Transactional

@Service
class DeviceDataService(
        private val deviceDataRepository: DeviceDataRepository,
        cacheManager: CacheManager
) {
    private val log by Logger()

    private val cache = cacheManager.getCache<Any, Any>(CachingAutoConfiguration.CACHE_DEVICE_DATA)

    fun getInt(device: Device, property: Property, invalidate: Boolean = false): Int? = getString(device, property, invalidate)?.toInt()

    fun getBytes(device: Device, property: Property, invalidate: Boolean = false): ByteArray? =
            Base64.getDecoder().decode(getString(device, property, invalidate))

    fun getString(device: Device, property: Property, invalidate: Boolean = false): String? {
        val key = property.name
        var value = cache.get(key) as String?

        if (value == null) {
            val entity = deviceDataRepository.findByDeviceIdAndProperty(device.id, key)
            value = entity?.value
            if (invalidate && entity != null)
                deviceDataRepository.delete(entity)
        } else if (invalidate) {
            cache.remove(key)
        }

        return value
    }

    @Transactional(value = Transactional.TxType.REQUIRED)
    fun set(device: Device, property: Property, value: Int) = set(device, property, value.toString())

    @Transactional(value = Transactional.TxType.REQUIRED)
    fun set(device: Device, property: Property, value: ByteArray) = set(device, property, Base64.getEncoder().encodeToString(value))

    @Transactional(value = Transactional.TxType.REQUIRED)
    fun set(device: Device, property: Property, value: String) {
        val key = property.name

        if (cache.containsKey(key)) {
            if (deviceDataRepository.replaceValue(device.id, key, value) == 0) {
                log.trace("Cache value for device ${device.id} and key $key didn't exist in the database but it did in the cache")
                deviceDataRepository.save(createEntity(device, property, value))
            }
        } else {
            deviceDataRepository.deleteByDeviceAndProperty(device, key)
            deviceDataRepository.save(createEntity(device, property, value))
        }

        cache.put(key, value)
    }

    private fun createEntity(device: Device, property: Property, value: String): DeviceData {
        val entity = DeviceData()
        entity.device = device
        entity.property = property.name
        entity.value = value
        return entity
    }

    enum class Property {
        DAILY_PRODUCTION,
        VENDOR_DATA
    }
}