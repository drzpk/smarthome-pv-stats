package dev.drzepka.pvstats.service

import dev.drzepka.pvstats.common.model.vendor.DeviceType
import dev.drzepka.pvstats.common.model.vendor.SofarData
import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.model.CurrentStats
import dev.drzepka.pvstats.model.DataSourceUserDetails
import dev.drzepka.pvstats.service.data.DailySummaryService
import dev.drzepka.pvstats.service.data.MeasurementService
import dev.drzepka.pvstats.util.Logger
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class StatsService(
        private val measurementService: MeasurementService,
        private val dailySummaryService: DailySummaryService,
        private val deviceDataService: DeviceDataService
) {
    private val log by Logger()

    fun getCurrentStats(): CurrentStats? {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as DataSourceUserDetails
        val device = userDetails.dataSource.device!!
        return when (device.type) {
            DeviceType.SMA -> getSMAStats(device)
            DeviceType.SOFAR -> getSofarStats(device)
            DeviceType.GENERIC -> null
        }
    }

    // todo: use polymorphism
    private fun getSMAStats(device: Device): CurrentStats {
        val lastMeasurement = measurementService.getLastMeasurement(device)
        val yesterdaySummary = dailySummaryService.getLastSummaryFor(device)
        val todayGeneration = if (yesterdaySummary != null) lastMeasurement.totalWh - yesterdaySummary.totalWh else 0
        val power = deviceDataService.getInt(device, DeviceDataService.Property.POWER)

        // todo: make this timeout configurable
        val powerValue = if (power != null && power.instant.isAfter(Instant.now().minusSeconds(600)))
            power.value
        else
            -1
        return CurrentStats(powerValue, device.name, todayGeneration)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getSofarStats(device: Device): CurrentStats {
        val rawData = deviceDataService.getBytes(device, DeviceDataService.Property.VENDOR_DATA)
        if (rawData == null) {
            log.debug("No vendor data for device $device")
            return CurrentStats(-1, device.name, -1)
        }

        val sofarData = SofarData(rawData.value.toTypedArray())
        val curremtPower = if (rawData.instant.isAfter(Instant.now().minusSeconds(600)))
            sofarData.currentPower
        else
            -1
        return CurrentStats(curremtPower, "", sofarData.energyToday, sofarData.pv1Voltage, sofarData.pv1Current)
    }
}