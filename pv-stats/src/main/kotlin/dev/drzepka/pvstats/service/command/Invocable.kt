package dev.drzepka.pvstats.service.command

interface Invocable {
    val name: String
    val description: String
    val group: CommandGroup?
}