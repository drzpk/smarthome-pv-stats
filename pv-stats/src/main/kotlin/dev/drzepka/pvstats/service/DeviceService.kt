package dev.drzepka.pvstats.service

import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.model.DeviceType
import dev.drzepka.pvstats.repository.DeviceRepository
import org.springframework.stereotype.Service

@Service
class DeviceService(private val deviceRepository: DeviceRepository) {

    fun getDevices(active: Boolean = true, inactive: Boolean = false): List<Device> {
        if (!active && !inactive)
            return emptyList()

        return if (active && !inactive)
            deviceRepository.findByActive(true)
        else if (!active && inactive)
            deviceRepository.findByActive(false)
        else
            deviceRepository.findAll().toList()
    }

    fun getDevice(id: Int): Device? = deviceRepository.findById(id).orElse(null)

    fun addDevice(name: String, description: String, type: DeviceType, apiUrl: String): Device {
        val device = Device()
        device.name = name
        device.description = description
        device.type = type
        device.apiUrl = apiUrl
        return deviceRepository.save(device)
    }

    fun getDeviceTypes(): List<DeviceType> = DeviceType.values().asList()
}