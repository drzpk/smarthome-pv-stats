package dev.drzepka.pvstats.service.command

interface Command : Invocable {
    val positionalArgCount: Int

    fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String>
    fun getArguments(): List<Argument>
}