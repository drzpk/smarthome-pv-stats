package dev.drzepka.pvstats.util

import dev.drzepka.pvstats.model.ValidationException
import java.net.URI

object ValidationUtils {

    fun length(name: String, value: String?, required: Boolean = false, minLength: Int? = null, maxLength: Int? = null) {
        if (value == null && required)
            throw ValidationException("$name is required")
        if (minLength != null && value != null && value.length < minLength)
            throw ValidationException("Minimum length of $name is $minLength, but ${value.length} was given")
        if (maxLength != null && value != null && value.length > maxLength)
            throw ValidationException("Maximum length of $name is $maxLength, but ${value.length} was given")
    }

    fun url(name: String, value: String?) {
        if (value == null) return

        try {
            URI.create(value)
        } catch (e: IllegalArgumentException) {
            throw ValidationException("$name is not valid url")
        }

        if (!value.startsWith("http://") && !value.startsWith("https://"))
            throw ValidationException("$name is not valid url")
    }
}