package dev.drzepka.pvstats.mock

import dev.drzepka.pvstats.entity.DataSource
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.model.DataSourceUserDetails
import dev.drzepka.pvstats.repository.DeviceRepository
import dev.drzepka.pvstats.repository.MeasurementRepository
import dev.drzepka.pvstats.util.MockLoader
import dev.drzepka.pvstats.web.DataController
import dev.drzepka.smarthome.common.pvstats.model.PutDataRequest
import dev.drzepka.smarthome.common.pvstats.model.sma.Entry
import dev.drzepka.smarthome.common.pvstats.model.sma.SMADashValues
import dev.drzepka.smarthome.common.pvstats.model.sma.SMADeviceData
import dev.drzepka.smarthome.common.pvstats.model.sma.SMAMeasurement
import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import dev.drzepka.smarthome.common.pvstats.model.vendor.SMAData
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.math.floor
import kotlin.random.Random

@Component
@Profile("mock")
class SMAMockLogger(
        private val measurementRepository: MeasurementRepository,
        deviceRepository: DeviceRepository,
        private val dataController: DataController
) {

    private val random = Random.Default
    private val mockAuthentication: Authentication

    init {
        val dataSource = DataSource()
        dataSource.device = deviceRepository.findByName(MockLoader.MOCK_DEVICE_NAME)

        mockAuthentication = TestingAuthenticationToken(DataSourceUserDetails(dataSource), "mock")
    }

    @Scheduled(cron = "0 0/1 * * * *")
    fun logData() {
        val data = SMAData(getMeasurement(), getDashValues())

        val request = PutDataRequest()
        request.type = DeviceType.SMA
        request.data = data.serialize()

        SecurityContextHolder.getContext().authentication = mockAuthentication
        dataController.putData(request)
    }

    private fun getMeasurement(): SMAMeasurement {
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

    private fun getDashValues(): SMADashValues = SMADashValues().apply {
        val randomPower = Random.Default.nextInt(500, 3000)
        this["result"] = mapOf<String, Any>(
                "some_device_name" to mapOf(
                        "6100_40263F00" to mapOf(
                                "1" to listOf(mapOf("val" to randomPower))
                        ),
                        "6800_10821E00" to mapOf(
                                "1" to listOf(mapOf("val" to "test SMA device"))
                        )
                )
        )
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