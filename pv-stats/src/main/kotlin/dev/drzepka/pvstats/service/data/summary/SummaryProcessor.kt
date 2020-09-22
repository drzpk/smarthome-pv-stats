package dev.drzepka.pvstats.service.data.summary

import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.entity.EnergyMeasurementDailySummary
import dev.drzepka.pvstats.util.Logger
import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import java.util.*
import kotlin.math.max

typealias Summary = EnergyMeasurementDailySummary

abstract class SummaryProcessor {
    abstract val deviceType: DeviceType

    private val log by Logger()

    abstract fun calculateSummary(previous: Summary?, current: Summary, data: List<EnergyMeasurement>)

    protected fun calculateAvgAndMaxPower(summary: Summary, data: List<EnergyMeasurement>) {
        var nonZeroPowerSum = 0
        var nonZeroPowerCount = 0
        var maxPower = 0

        var previousTimestamp: Date? = null
        data.forEach {
            val duplicate = it.timestamp.equals(previousTimestamp)
            previousTimestamp = it.timestamp
            if (duplicate) {
                log.warn("Duplicated measurement ${it.id} at ${it.timestamp} detected. ${summary.device}")
                return@forEach
            }

            if (it.powerW > 0) {
                nonZeroPowerSum += it.powerW
                nonZeroPowerCount++
                maxPower = max(maxPower, it.powerW)
            }
        }

        summary.maxPower = maxPower
        summary.avgPower = if (nonZeroPowerCount > 0) (nonZeroPowerSum.toFloat() / nonZeroPowerCount) else 0f
    }
}