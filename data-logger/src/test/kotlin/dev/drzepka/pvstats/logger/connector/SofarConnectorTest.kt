package dev.drzepka.pvstats.logger.connector

import dev.drzepka.pvstats.common.model.vendor.SofarData
import dev.drzepka.pvstats.common.model.vendor.VendorType
import dev.drzepka.pvstats.logger.model.config.SourceConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class SofarConnectorTest {

    @Test
    fun `check generating socket request data`() {
        val connector = SofarConnector()
        val data = connector.getSocketRequestData(getConfig(1629384756))
        val expected = Connector.hexStringToBytes("a517001045000034701e6102000000000000000000000000000001030000002705d09115")
        Assertions.assertArrayEquals(expected, data)
    }

    @Test
    fun `check parsing response data`() {
        /*
            E Today : 23.55   Total: 64.05
            H Total :    44   Temp : module: 38.0  inner: 38.0
            P active:   2.4   react: 0.65
            PV1   V: 387.4   I: 6.37  P: 2.47
            PV2   V:  79.1   I: 0.01  P:  0.0
            L1    P:  0.84   V: 244.1   I: 3.44   F: 49.98
            L2    P:  0.83   V: 240.9   I: 3.44   F: 49.98
            L3    P:  0.82   V: 238.7   I: 3.43   F: 49.98
         */

        val raw = Connector.hexStringToBytes("a5610010150072f3a0386602018e8002009c2400006232b4" +
                "5e01034e0002000000000000000000000f22027d0317000100f7000000f00041138609890158096901580953015700" +
                "0000400000002c093302800026003219e00f18031d003c000000010000054d087206cdccad0315")

        val service = SofarConnector()
        val parsed = service.parseSocketResponseData(getConfig(123), raw) as SofarData

        Assertions.assertEquals(23550, parsed.energyToday)
        Assertions.assertEquals(64000, parsed.energyTotal)
        Assertions.assertEquals(2400, parsed.currentPower)
        Assertions.assertEquals(44, parsed.generationHoursTotal)
        Assertions.assertEquals(49.98f, parsed.frequency, 0.05f)

        Assertions.assertEquals(387.4f, parsed.pv1Voltage, 0.05f)
        Assertions.assertEquals(6.37f, parsed.pv1Current, 0.05f)
        Assertions.assertEquals(2470, parsed.pv1Power)
        Assertions.assertEquals(79.1f, parsed.pv2Voltage, 0.05f)
        Assertions.assertEquals(0.01f, parsed.pv2Current, 0.05f)
        Assertions.assertEquals(0, parsed.pv2Power)

        Assertions.assertEquals(244.1f, parsed.phaseAVoltage, 0.05f)
        Assertions.assertEquals(3.44f, parsed.phaseACurrent, 0.05f)
        Assertions.assertEquals(240.9f, parsed.phaseBVoltage, 0.05f)
        Assertions.assertEquals(3.44f, parsed.phaseBCurrent, 0.05f)
        Assertions.assertEquals(238.7f, parsed.phaseCVoltage, 0.05f)
        Assertions.assertEquals(3.43f, parsed.phaseCCurrent, 0.05f)

    }

    private fun getConfig(sn: Int): SourceConfig = SourceConfig("name", VendorType.SOFAR, "localhost", sn, 3, 60)
}