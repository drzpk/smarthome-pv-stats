package dev.drzepka.pvstats.service.data

import dev.drzepka.pvstats.service.data.measurement.MeasurementProcessor
import dev.drzepka.pvstats.service.data.summary.SummaryProcessor
import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import dev.drzepka.smarthome.common.pvstats.model.vendor.VendorData
import org.springframework.stereotype.Service

@Service
class HandlerResolverService(
        measurementProcessors: List<MeasurementProcessor<out VendorData>>,
        summaryProcessors: List<SummaryProcessor>
) {
    private val measurementProcessors = measurementProcessors.map { Pair(it.deviceType, it) }.toMap()
    private val summaryProcessors = summaryProcessors.map { Pair(it.deviceType, it) }.toMap()

    fun measurement(type: DeviceType): MeasurementProcessor<out VendorData>? = measurementProcessors[type]

    fun summary(type: DeviceType): SummaryProcessor? = summaryProcessors[type]
}