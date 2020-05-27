package dev.drzepka.pvstats.service.data.summary

import dev.drzepka.pvstats.common.model.vendor.DeviceType
import dev.drzepka.pvstats.common.model.vendor.SofarData
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.service.DeviceDataService
import dev.drzepka.pvstats.util.Logger
import org.springframework.stereotype.Component

@Component
class SofarSummaryProcessor(private val deviceDataService: DeviceDataService) : SummaryProcessor() {
    override val deviceType = DeviceType.SOFAR

    private val log by Logger()

    override fun calculateSummary(previous: Summary?, current: Summary, data: List<EnergyMeasurement>) {
        val previousTotalWh = previous?.totalWh ?: 0
        current.totalWh = if (data.isNotEmpty()) data.last().totalWh else previousTotalWh

        val rawData = deviceDataService.getBytes(current.device, DeviceDataService.Property.VENDOR_DATA, true)
        if (rawData != null) {
            // Use more accurate data
            val sofarData = SofarData(rawData.toTypedArray())
            current.deltaWh = sofarData.energyToday
        } else {
            log.warn("Vendor data not available, deltaWh will be less accurate")
            current.deltaWh = current.totalWh - previousTotalWh
        }

        calculateAvgAndMaxPower(current, data)
    }
}