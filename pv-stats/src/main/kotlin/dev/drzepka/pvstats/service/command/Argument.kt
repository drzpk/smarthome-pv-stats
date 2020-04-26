package dev.drzepka.pvstats.service.command

data class Argument(
        val name: String,
        val description: String,
        val hasValue: Boolean,
        val required: Boolean = false
)