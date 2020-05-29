@file:Suppress("UNCHECKED_CAST")

package dev.drzepka.pvstats.util

import org.mockito.Mockito

fun <T> kAny(): T {
    Mockito.any<T>()
    return null as T
}

fun <T> kEq(value: T): T {
    Mockito.eq(value)
    return null as T
}