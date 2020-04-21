package dev.drzepka.pvstats.model.device.sma

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import dev.drzepka.pvstats.util.TimestampDeserializer
import java.util.*
import kotlin.collections.HashMap

class SMAMeasurement {
    var result = HashMap<String, SMADeviceData>()
}

class SMADeviceData {
    @JsonProperty("7000")
    var currentData = HashMap<String, List<Entry>>()
}

class Entry constructor() {
    @JsonDeserialize(using = TimestampDeserializer::class)
    var t: Date = Date()
    var v: Int? = null

    constructor(t: Date = Date(), v: Int?) : this() {
        this.t = t
        this.v = v
    }
}
