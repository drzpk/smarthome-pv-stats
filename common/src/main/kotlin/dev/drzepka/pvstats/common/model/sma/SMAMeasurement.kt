package dev.drzepka.pvstats.common.model.sma

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import dev.drzepka.pvstats.common.util.SecondTimestampDeserializer
import dev.drzepka.pvstats.common.util.SecondTimestampSerializer
import java.util.*
import kotlin.collections.HashMap

class SMAMeasurement {
    var result = HashMap<String, SMADeviceData>()

    @JsonIgnore
    fun getEntries(): List<Entry> {
        val deviceKey = result.keys.firstOrNull() ?: throw IllegalStateException("Device key is null")

        val deviceData = result[deviceKey]
        val currentData = deviceData!!.currentData

        return currentData[TODAY_MEASUREMENTS_SUBKEY]
                ?: throw IllegalStateException("Missing list subsection 1")
    }

    companion object {
        private const val TODAY_MEASUREMENTS_SUBKEY = "1" // device class
    }
}

class SMADeviceData {
    @JsonProperty("7000")
    var currentData = HashMap<String, List<Entry>>()
}

class Entry constructor() {
    @JsonSerialize(using = SecondTimestampSerializer::class)
    @JsonDeserialize(using = SecondTimestampDeserializer::class)
    var t: Date = Date()
    var v: Int? = null

    constructor(t: Date = Date(), v: Int?) : this() {
        this.t = t
        this.v = v
    }
}
