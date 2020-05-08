package dev.drzepka.pvstats.web.client.sma

import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.model.device.sma.Entry
import dev.drzepka.pvstats.model.device.sma.SMADashValues
import dev.drzepka.pvstats.model.device.sma.SMADeviceData
import dev.drzepka.pvstats.model.device.sma.SMAMeasurement
import dev.drzepka.pvstats.repository.MeasurementRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor
import kotlin.random.Random

@Component
@Profile("mock")
class SMAMockClient(private val measurementRepository: MeasurementRepository) : SMAApiClient {

    private val random = Random.Default

    override fun getDashLogger(uri: URI): SMAMeasurement {
        val now = LocalDateTime.now()

        val lastMeasurement = getLastMeasurement()
        val lastEntry = Entry(lastMeasurement.timestamp, lastMeasurement.totalWh)

        val lastTime = LocalDateTime.ofInstant(lastMeasurement.timestamp.toInstant(), ZoneId.systemDefault())
        val diffSeconds = now.atZone(ZoneId.systemDefault()).toInstant().epochSecond -
                lastTime.atZone(ZoneId.systemDefault()).toInstant().epochSecond

        val iterations = floor(diffSeconds / INTERVAL.seconds.toDouble()).toInt()
        val entries = ArrayList<Entry>()
        entries.ensureCapacity(iterations)

        var previous = lastEntry
        var date = lastMeasurement.timestamp.toInstant()
        repeat(iterations) {
            val current = getRandomEntry(previous)
            current.t = Date.from(date)
            entries.add(current)
            previous = current
            date = date.plusSeconds(INTERVAL.seconds)
        }

        return createData(entries)
    }

    override fun getDashValues(uri: URI): SMADashValues {
        return object : SMADashValues() {
            override fun getPower(): Int = Random.Default.nextInt(500, 3000)
            override fun getDeviceName(): String = "Test SMA device"
        }
    }

    private fun getRandomEntry(previous: Entry): Entry {
        val delta = random.nextInt(1, 20)
        return Entry(v = (previous.v ?: 0) + delta)
    }

    private fun createData(entries: List<Entry>): SMAMeasurement {
        val subsection = hashMapOf(Pair("1", entries))
        val deviceData = SMADeviceData()
        deviceData.currentData = subsection
        val root = SMAMeasurement()
        root.result["device-id"] = deviceData
        return root
    }

    private fun getLastMeasurement(): EnergyMeasurement {
        var lastMeasurement = measurementRepository.findLast()
        if (lastMeasurement == null) {
            lastMeasurement = EnergyMeasurement()
            // Mock data is generated based on last measurement's time
            lastMeasurement.timestamp = Date.from(Instant.now().minusSeconds(10))
        }

        return lastMeasurement
    }

    companion object {
        private val INTERVAL = Duration.ofSeconds(10)
    }
}