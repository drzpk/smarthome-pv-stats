package dev.drzepka.pvstats.model.device.sma

import dev.drzepka.pvstats.loadTestJsonData
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class SMADashValuesTest {

    @Test
    fun `check getting power`() {
        val power = getTestObject().getPower()
        then(power).isEqualTo(1639)
    }

    @Test
    fun `check getting null power`() {
       val power = getTestObjectNullPower().getPower()
        then(power).isEqualTo(0)
    }

    @Test
    fun `check getting device name`() {
        val deviceName=  getTestObject().getDeviceName()
        then(deviceName).isEqualTo("STP4.0-3AV-40 752")
    }

    private fun getTestObject(): SMADashValues = loadTestJsonData("sma_dash_values")

    private fun getTestObjectNullPower(): SMADashValues = loadTestJsonData("sma_dash_values_null_power")
}