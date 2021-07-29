package dev.drzepka.pvstats.common.model.vendor

import java.time.Instant
import java.util.*
import kotlin.math.floor

/**
 * Response format source: official Sofar datasheet
 */
@Suppress("unused")
class SofarData(val raw: Array<Byte>) : VendorData() {

    val energyToday: Int
        get() = floor(getShort(TODAY_PRODUCTION, 1) * 10).toInt()
    val energyTotal: Int
        get() = getInt(TOTAL_PRODUCTION) * 1000
    val currentPower: Int // Watt
        get() = floor(getShort(ACTIVE_POWER, 1) * 10).toInt()
    val frequency: Float
        get() = getShort(GRID_FREQUENCY, 100)

    val generationHoursToday: Float
        get() = getShort(TODAY_GENERATION_TIME, 60)
    val generationHoursTotal: Int
        get() = getInt(TOTAL_GENERATION_TIME)

    val pv1Voltage: Float
        get() = getShort(PV1_VOLTAGE, 10)
    val pv1Current: Float
        get() = getShort(PV1_CURRENT, 100)
    val pv1Power: Int // Watt
        get() = floor(getShort(PV1_POWER, 1) * 10).toInt()
    val pv2Voltage: Float
        get() = getShort(PV2_VOLTAGE, 10)
    val pv2Current: Float
        get() = getShort(PV2_CURRENT, 100)
    val pv2Power: Int // Watt
        get() = floor(getShort(PV2_POWER, 1) * 10).toInt()

    val phaseAVoltage: Float
        get() = getShort(PHASE_A_VOLTAGE, 10)
    val phaseACurrent: Float
        get() = getShort(PHASE_A_CURRENT, 100)
    val phaseBVoltage: Float
        get() = getShort(PHASE_B_VOLTAGE, 10)
    val phaseBCurrent: Float
        get() = getShort(PHASE_B_CURRENT, 100)
    val phaseCVoltage: Float
        get() = getShort(PHASE_C_VOLTAGE, 10)
    val phaseCCurrent: Float
        get() = getShort(PHASE_C_CURRENT, 100)

    private fun getShort(offset: Int, divider: Int): Float =
            raw[offset].toInt().and(0xff).shl(8).or(raw[offset + 1].toInt().and(0xff)).toFloat() / divider

    private fun getInt(offset: Int): Int =
            raw[offset].toInt().and(0xff).shl(24)
                    .or(raw[offset + 1].toInt().and(0xff).shl(16))
                    .or(raw[offset + 2].toInt().and(0xff).shl(8))
                    .or(raw[offset + 3].toInt().and(0xff))

    override fun serialize(): Any = Instant.now().toEpochMilli().toString() + SERIALIZATION_SEPARATOR + Base64.getEncoder().encodeToString(raw.toByteArray())

    companion object Offsets {
        fun deserialize(data: Any): SofarData {
            if (data !is String)
                throw IllegalArgumentException("Unknown data type: ${data::class.java.simpleName}")

            val split = data.split(SERIALIZATION_SEPARATOR)
            val raw = Base64.getDecoder().decode(split[1])

            return SofarData(raw.toTypedArray())
        }

        private const val SERIALIZATION_SEPARATOR = ":"

        // Basic info
        private const val OPERATING_STATE = 1
        private const val FAULT_1 = 3
        private const val FAULT_2 = 5
        private const val FAULT_3 = 7
        private const val FAULT_4 = 9
        private const val FAULT_5 = 11

        // Grid input data
        private const val PV1_VOLTAGE = 13 // Unit: 0.1V
        private const val PV1_CURRENT = 15 // Unit: 0.01A
        private const val PV2_VOLTAGE = 17 // Unit: 0.1V
        private const val PV2_CURRENT = 19 // Unit: 0.01A
        private const val PV1_POWER = 21 // Unit: 0.01kW
        private const val PV2_POWER = 23 // Unit: 0.01kW

        // Grid output data
        private const val ACTIVE_POWER = 25 // Unit: 0.01kW
        private const val REACTIVE_POWER = 27 // Unit: 0.01kVar
        private const val GRID_FREQUENCY = 29 // Unit: 0.01Hz
        private const val PHASE_A_VOLTAGE = 31 // Unit: 0.1V
        private const val PHASE_A_CURRENT = 33 // Unit: 0.01A
        private const val PHASE_B_VOLTAGE = 35 // Unit: 0.1V
        private const val PHASE_B_CURRENT = 37 // Unit: 0.01A
        private const val PHASE_C_VOLTAGE = 39 // Unit: 0.1V
        private const val PHASE_C_CURRENT = 41 // Unit: 0.01A

        // Power generation data
        private const val TOTAL_PRODUCTION = 43 // Unit: 1kWh
        private const val TOTAL_GENERATION_TIME = 47 // Unit: 1hour
        private const val TODAY_PRODUCTION = 51 // Unit: 0.01kWh
        private const val TODAY_GENERATION_TIME = 53

        // Internal inverter data
        private const val INVERTER_MODULE_TEMPERATURE = 55
        private const val INVERTER_INNER_TEMPERATURE = 57
        private const val INVERTER_BUS_VOLTAGE = 59
        private const val PV1_VOLTAGE_SAMPLE_BY_SLAVE_CPU = 61 // Unit: 0.1V
        private const val PV1_VOLGATE_SAMPLE_BY_SLAVE_CPU = 63 // Unit: 0.01A
        private const val COUNTDOWN_TIME = 65
        private const val ALERT_MESSAGE = 67
        private const val INPUT_MODE = 69 // 0x00 - in parallel, 0x01 - independent
        private const val COMMUNICATION_BOARD = 71
        private const val INSULATION_PV1_PLUS_TO_GROUND = 73
        private const val INSULATION_PV_MINUS_TO_GROUND = 75
        private const val COUNTRY = 77
    }
}