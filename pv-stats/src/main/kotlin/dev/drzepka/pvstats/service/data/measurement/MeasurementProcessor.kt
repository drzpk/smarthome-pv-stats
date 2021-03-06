package dev.drzepka.pvstats.service.data.measurement

import com.fasterxml.jackson.databind.ObjectMapper
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.model.DataSourceUserDetails
import dev.drzepka.pvstats.util.Logger
import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import dev.drzepka.smarthome.common.pvstats.model.vendor.VendorData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder

abstract class MeasurementProcessor<T : VendorData> {
    abstract val deviceType: DeviceType

    private val log by Logger()

    private lateinit var objectMapper: ObjectMapper

    fun process(deviceType: DeviceType, data: Any) {
        if (deviceType == DeviceType.GENERIC)
            throw IllegalArgumentException("Cannot use generic device for data logging")

        val userDetails = SecurityContextHolder.getContext().authentication.principal as DataSourceUserDetails
        val device = userDetails.dataSource.device!!

        if (deviceType != device.type)
            throw IllegalArgumentException("Device type from request ($deviceType) doesn't match device type of authenticated user (${device.type})")

        val deserialized = try {
            deserialize(data)
        } catch (e: Exception) {
            log.error("Data deserialization for device $device failed", e)
            if (log.isTraceEnabled)
                log.trace("Serialized data: $data")
            throw e
        }

        try {
            process(device, deserialized)
        } catch (e: Exception) {
            log.error("Error while processing measurement for $device", e)
        }
    }

    @Autowired
    fun setObjectMapper(objectMapper: ObjectMapper) {
        this.objectMapper = objectMapper
    }

    abstract fun process(device: Device, data: T)

    protected abstract fun deserialize(data: Any): T
}