package dev.drzepka.pvstats.service

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.DeviceData
import dev.drzepka.pvstats.repository.DeviceDataRepository
import dev.drzepka.pvstats.util.MockCache
import dev.drzepka.pvstats.util.kAny
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import javax.cache.CacheManager

class DeviceDataServiceTest {

    private val deviceDataRepository = mock<DeviceDataRepository> {
        on { save(kAny()) } doAnswer { it.arguments[0] as DeviceData }
    }
    private val cacheManager = mock<CacheManager> {
        on { getCache<Any, Any>(Mockito.anyString()) } doReturn MockCache()
    }

    @Test
    fun `check storing cache for multiple devices`() {
        val device1 = getDevice()
        val device2 = getDevice()

        val service = getService()
        service.set(device1, DeviceDataService.Property.DAILY_PRODUCTION, 1000)
        service.set(device2, DeviceDataService.Property.DAILY_PRODUCTION, 2000)

        val wrapper1 = service.getInt(device1, DeviceDataService.Property.DAILY_PRODUCTION)
        val wrapper2 = service.getInt(device2, DeviceDataService.Property.DAILY_PRODUCTION)
        then(wrapper1?.value).isEqualTo(1000)
        then(wrapper2?.value).isEqualTo(2000)
    }

    private fun getDevice(): Device = Device().apply {
        id = Random().nextInt()
        name = "$${id}_name"
    }

    private fun getService(): DeviceDataService = DeviceDataService(deviceDataRepository, cacheManager)
}