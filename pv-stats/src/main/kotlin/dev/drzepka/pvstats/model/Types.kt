package dev.drzepka.pvstats.model

import java.time.Instant

data class InstantValue<T>(val value: T, val instant: Instant)