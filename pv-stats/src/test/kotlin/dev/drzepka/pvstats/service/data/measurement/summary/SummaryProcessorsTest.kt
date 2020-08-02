package dev.drzepka.pvstats.service.data.measurement.summary

import com.nhaarman.mockitokotlin2.mock
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.entity.EnergyMeasurementDailySummary
import dev.drzepka.pvstats.service.DeviceDataService
import dev.drzepka.pvstats.service.data.summary.SMASummaryProcessor
import dev.drzepka.pvstats.service.data.summary.SofarSummaryProcessor
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

class SummaryProcessorsTest {

    private val deviceDataService = mock<DeviceDataService> {}

    private val summaryProcessors = listOf(
            SMASummaryProcessor(),
            SofarSummaryProcessor(deviceDataService)
    )

    @Test
    fun `should calculate summary for a single record`() {
        summaryProcessors.forEach {
            println("Checking ${it.deviceType}")

            val data = listOf(getMeasurement(LocalTime.now(), 100, 50, 2))
            val current = getSummaryEntity()

            it.calculateSummary(EnergyMeasurementDailySummary(), current, data)

            then(current.totalWh).isEqualTo(100)
            then(current.deltaWh).isEqualTo(100)
            then(current.avgPower).isEqualTo(2f)
            then(current.maxPower).isEqualTo(2)
        }
    }

    @Test
    fun `should calculate summary for multiple records`() {
        summaryProcessors.forEach {
            println("Checking ${it.deviceType}")

            val now = LocalTime.now()
            val data = listOf(
                    getMeasurement(now.minusMinutes(5), 100, 100, 1),
                    getMeasurement(now, 200, 100, 2),
                    getMeasurement(now.plusMinutes(5), 300, 100, 3)
            )

            val previous = EnergyMeasurementDailySummary()
            previous.totalWh = 100
            val current = EnergyMeasurementDailySummary()

            it.calculateSummary(previous, current, data)

            then(current.totalWh).isEqualTo(300)
            then(current.deltaWh).isEqualTo(200)
            then(current.avgPower).isEqualTo(2f, Offset.offset(0.2f))
            then(current.maxPower).isEqualTo(3)
        }
    }


    @Test
    fun `should calculate summary with no time difference`() {
        // Duplicated records should be excluded from calculation
        summaryProcessors.forEach {
            val now = LocalTime.now()
            val data = listOf(
                    getMeasurement(now, 100, 50, 10),
                    getMeasurement(now, 200, 50, 20)
            )
            val current = getSummaryEntity()

            it.calculateSummary(EnergyMeasurementDailySummary(), current, data)

            then(current.avgPower).isEqualTo(10f)
            then(current.maxPower).isEqualTo(10)
        }
    }


    @Test
    fun `should calculate summary with no data`() {
        summaryProcessors.forEach {
            val data = emptyList<EnergyMeasurement>()
            val summary = getSummaryEntity()

            it.calculateSummary(EnergyMeasurementDailySummary(), summary, data)

            then(summary.totalWh).isEqualTo(0)
            then(summary.deltaWh).isEqualTo(0)
            then(summary.avgPower).isEqualTo(0f)
            then(summary.maxPower).isEqualTo(0)
        }
    }

    private fun getSummaryEntity(): EnergyMeasurementDailySummary {
        return EnergyMeasurementDailySummary()
    }

    private fun getMeasurement(time: LocalTime, totalWh: Int, deltaWh: Int, power: Int): EnergyMeasurement {
        val measurement = EnergyMeasurement()
        measurement.timestamp = Date.from(LocalDateTime.now().with(time).atZone(ZoneId.systemDefault()).toInstant())
        measurement.totalWh = totalWh
        measurement.deltaWh = deltaWh
        measurement.powerW = power
        return measurement
    }
}