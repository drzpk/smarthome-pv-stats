package dev.drzepka.pvstats.util

import dev.drzepka.pvstats.common.model.vendor.DeviceType
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.repository.DeviceRepository
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
        if (deviceRepository.findById(MOCK_DEVICE_ID).isPresent)
            return

        log.info("Mock device doesn't exist, creating")

        val device = Device()
        device.id = MOCK_DEVICE_ID
        device.name = "mock device"
        device.type = DeviceType.SMA
        deviceRepository.save(device)
    }

    companion object {
        private const val MOCK_DEVICE_ID = 0xdead
    }
}