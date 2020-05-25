package dev.drzepka.pvstats.logger.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalTime

class TimeUtilsTest {

    @Test
    fun `check padding calculation`() {
        val now = LocalTime.of(11, 4, 23)

        Assertions.assertEquals(2, roundAndGetDelay(5, now))
        Assertions.assertEquals(7, roundAndGetDelay(10, now))
        Assertions.assertEquals(7, roundAndGetDelay(15, now))
        Assertions.assertEquals(7, roundAndGetDelay(30, now))
        Assertions.assertEquals(37, roundAndGetDelay(60, now))
        Assertions.assertEquals(3, roundAndGetDelay(13, now))
    }
}