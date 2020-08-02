package dev.drzepka.pvstats.service

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import dev.drzepka.pvstats.common.model.vendor.DeviceType
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.model.ApplicationException
import dev.drzepka.pvstats.repository.DeviceRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*

class DeviceServiceTest {

    private val deviceRepository = mock<DeviceRepository> {
        on { findById(Mockito.eq(1)) } doReturn Optional.of(Device())
        on { save(Mockito.any()) } doReturn Device()
    }

    @Test
    fun `should do validation on device creation`() {
        val service = getService()

        Assertions.assertThrows(ApplicationException::class.java) {
            service.addDevice("", "name is empty", DeviceType.SMA)
        }

        Assertions.assertThrows(ApplicationException::class.java) {
            service.addDevice("description too long".repeat(50), "asdf", DeviceType.SMA)
        }
    }

    @Test
    fun `should save device entity`() {
        val service = getService()
        service.addDevice("name", "description", DeviceType.SMA)
        verify(deviceRepository, Mockito.times(1)).save(Mockito.any())
    }

    @Test
    fun `should not save entity when no properties were modified`() {
        val service = getService()
        service.modifyDevice(1, null, null, null)
        verify(deviceRepository, Mockito.times(0)).save(Mockito.any(Device::class.java))
    }

    private fun getService(): DeviceService = DeviceService(deviceRepository)
}