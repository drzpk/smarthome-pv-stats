package dev.drzepka.pvstats.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.util.*
import java.util.concurrent.TimeUnit

class TimestampDeserializer : JsonDeserializer<Date>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Date = Date(TimeUnit.SECONDS.toMillis(p!!.text.toLong()))
}