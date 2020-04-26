package dev.drzepka.pvstats.service.command

interface Command : Invocable {
    fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String>
    fun getArguments(): List<Argument>

    fun positionalArgCount(): Int = 0
    fun positionalArgsUsage(): String = ""
}