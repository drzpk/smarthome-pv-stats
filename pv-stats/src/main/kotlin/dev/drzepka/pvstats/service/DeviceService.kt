package dev.drzepka.pvstats.service

import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.model.ApplicationException
import dev.drzepka.pvstats.model.DeviceType
import dev.drzepka.pvstats.repository.DeviceRepository
import dev.drzepka.pvstats.util.Logger
import dev.drzepka.pvstats.util.ValidationUtils
import org.springframework.stereotype.Service

@Service
class DeviceService(private val deviceRepository: DeviceRepository) {

    private val log by Logger()

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
        validateName(name, true)
        validateDescription(description, true)
        validateUrl(apiUrl, true)

        val device = Device()
        device.name = name
        device.description = description
        device.type = type
        device.apiUrl = apiUrl
        return deviceRepository.save(device)
    }

    fun modifyDevice(id: Int, name: String?, description: String?, apiUrl: String?) {
        val device = deviceRepository.findById(id).orElseThrow {
            return@orElseThrow ApplicationException("device with id $id wasn't found")
        }

        validateName(name, false)
        validateDescription(description, false)
        validateUrl(apiUrl, false)

        var modified = false
        if (name != null) {
            device.name = name
            modified = true
        }
        if (description != null) {
            device.description = description
            modified = true
        }
        if (apiUrl != null) {
            device.apiUrl = apiUrl
            modified = true
        }

        if (modified) {
            log.info("Updating device $id (${device.name}")
            deviceRepository.save(device)
        } else {
            log.info("Device won't be updated, no properties were modified")
        }
    }

    fun getDeviceTypes(): List<DeviceType> = DeviceType.values().asList().minus(DeviceType.UNKNOWN)

    private fun validateName(value: String?, required: Boolean) =
            ValidationUtils.length("name", value, required, minLength = 1, maxLength = 128)

    private fun validateDescription(value: String?, required: Boolean) =
            ValidationUtils.length("description", value, required, minLength = 1, maxLength = 255)

    private fun validateUrl(value: String?, required: Boolean) {
        ValidationUtils.length("api url", value, required, minLength = 1, maxLength = 128)
        ValidationUtils.url("api url", value)
    }
}