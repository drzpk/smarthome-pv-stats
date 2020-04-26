package dev.drzepka.pvstats.util

import dev.drzepka.pvstats.model.CommandException

object CommandUtils {

    fun checkArgLength(argName: String, value: String, minLength: Int? = null, maxLength: Int? = null) {
        if (minLength != null && value.length < minLength)
            throw CommandException("Minimum length of argument $argName is $minLength, but ${value.length} was given")
        if (maxLength != null && value.length > maxLength)
            throw CommandException("Maximum length of argument $argName is $maxLength, but ${value.length} was given")
    }

    fun error(text: String) = arrayOf("Error: $text")
}