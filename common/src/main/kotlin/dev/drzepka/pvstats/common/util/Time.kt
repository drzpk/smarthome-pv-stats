package dev.drzepka.pvstats.common.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun Instant.toLocalDate(): LocalDate = this.atZone(ZoneId.systemDefault()).toLocalDate()