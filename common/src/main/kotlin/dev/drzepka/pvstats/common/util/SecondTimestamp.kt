package dev.drzepka.pvstats.common.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.util.*

/**
 * Serializes Date with precision of seconds
 */
class SecondTimestampSerializer : JsonSerializer<Date>() {
    override fun serialize(value: Date, gen: JsonGenerator?, serializers: SerializerProvider?) {
        return gen?.writeNumber(value.time / 1000) ?: Unit
    }
}

/**
 * Deserializes Date with precision of seconds
 */
class SecondTimestampDeserializer : JsonDeserializer<Date>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Date = Date(p!!.text.toLong() * 1000)
}