package dev.drzepka.pvstats.service.data

import com.fasterxml.jackson.databind.ObjectMapper
import dev.drzepka.pvstats.common.model.vendor.VendorData
import dev.drzepka.pvstats.common.model.vendor.VendorType
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.model.DataSourceUserDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

abstract class DataProcessorService<T : VendorData>(private val clazz: KClass<T>) {
    abstract val vendorType: VendorType

    private lateinit var objectMapper: ObjectMapper

    fun process(dataBase64: String) {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as DataSourceUserDetails
        val device = userDetails.dataSource.device!!

        val decoded = Base64.getDecoder().decode(dataBase64).toTypedArray()
        val instance = clazz.primaryConstructor!!.call(decoded)
        process(device, instance)
    }

    @Autowired
    fun setObjectMapper(objectMapper: ObjectMapper) {
        this.objectMapper = objectMapper
    }

    protected abstract fun process(device: Device, data: T)
}