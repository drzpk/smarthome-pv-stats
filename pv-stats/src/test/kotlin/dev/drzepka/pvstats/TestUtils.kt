package dev.drzepka.pvstats

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper

inline fun <reified T> loadTestJsonData(resourceName: String): T {
    var fileName = resourceName
    if (!fileName.endsWith(".json"))
        fileName += ".json"

    val mapper = ObjectMapper()
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    return mapper.readValue(PvStatsApplication::class.java.classLoader.getResourceAsStream(fileName), T::class.java)
}