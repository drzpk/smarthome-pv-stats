package dev.drzepka.pvstats.service.data.summary

import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.service.DeviceDataService
import dev.drzepka.pvstats.util.Logger
import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import org.springframework.stereotype.Component

@Component
class SofarSummaryProcessor(private val deviceDataService: DeviceDataService) : SummaryProcessor() {
    override val deviceType = DeviceType.SOFAR

    private val log by Logger()

    override fun calculateSummary(previous: Summary?, current: Summary, data: List<EnergyMeasurement>) {
        val previousTotalWh = previous?.totalWh ?: 0
        current.totalWh = if (data.isNotEmpty()) data.last().totalWh else previousTotalWh

        val wrapper = deviceDataService.getInt(current.device, DeviceDataService.Property.DAILY_PRODUCTION, true)
        if (wrapper != null) {
            current.deltaWh = wrapper.value
        } else {
            log.warn("Vendor data not available, deltaWh will be less accurate")
            current.deltaWh = current.totalWh - previousTotalWh
        }

        calculateAvgAndMaxPower(current, data)
    }
}