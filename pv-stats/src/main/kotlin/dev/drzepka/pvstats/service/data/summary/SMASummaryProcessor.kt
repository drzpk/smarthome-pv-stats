package dev.drzepka.pvstats.service.data.summary

import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import org.springframework.stereotype.Component

@Component
class SMASummaryProcessor : SummaryProcessor() {
    override val deviceType = DeviceType.SMA

    override fun calculateSummary(previous: Summary?, current: Summary, data: List<EnergyMeasurement>) {
        val previousTotalWh = previous?.totalWh ?: 0
        current.totalWh = if (data.isNotEmpty()) data.last().totalWh else previousTotalWh
        current.deltaWh = current.totalWh - previousTotalWh

       calculateAvgAndMaxPower(current, data)
    }
}