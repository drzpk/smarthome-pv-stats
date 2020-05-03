package dev.drzepka.pvstats.model

open class ApplicationException(message: String) : RuntimeException(message)

class CommandException(message: String) : ApplicationException(message)

class ValidationException(message: String) : ApplicationException(message)