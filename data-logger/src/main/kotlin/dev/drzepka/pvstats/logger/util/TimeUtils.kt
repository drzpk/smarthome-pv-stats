package dev.drzepka.pvstats.logger.util

import java.time.LocalTime
import kotlin.math.ceil

/**
 * Returns difference between current second in minute and smallest greater than or equal second of given time.
 */
fun roundAndGetDelay(intervalSeconds: Int, now: LocalTime = LocalTime.now()): Int =
        (ceil(now.second / intervalSeconds.toFloat()) * intervalSeconds).toInt() - now.second