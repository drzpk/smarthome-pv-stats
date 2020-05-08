package dev.drzepka.pvstats.util

import org.mockito.Mockito

@Suppress("UNCHECKED_CAST")
fun <T> kAny(): T {
    Mockito.any<T>()
    return null as T
}