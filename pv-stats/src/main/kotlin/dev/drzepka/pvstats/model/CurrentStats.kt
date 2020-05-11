package dev.drzepka.pvstats.model

data class CurrentStats(
        val power: Int,
        val deviceName: String,
        val generationToday: Int,
        val inverterVoltage: Float = 0f,
        val inverterCurrent: Float = 0f
)