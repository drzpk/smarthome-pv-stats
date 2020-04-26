package dev.drzepka.pvstats.model

open class ApplicationException(message: String) : Exception(message)

class CommandException(message: String) : ApplicationException(message)