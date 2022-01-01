package dev.drzepka.pvstats.util

import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.repository.DeviceRepository
import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@Profile("mock")
class MockLoader(private val deviceRepository: DeviceRepository) {

    private val log by Logger()

    @PostConstruct
    fun initializeMocks() {
        createMockDeviceIfNotExists()
    }

    private fun createMockDeviceIfNotExists() {
        if (deviceRepository.findByName(MOCK_DEVICE_NAME) != null)
            return

        log.info("Mock device doesn't exist, creating")

        val device = Device()
        device.name = MOCK_DEVICE_NAME
        device.type = DeviceType.SMA
        deviceRepository.save(device)
    }

    companion object {
        const val MOCK_DEVICE_NAME = "__mock_device__"
    }
}