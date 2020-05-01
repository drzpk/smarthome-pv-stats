package dev.drzepka.pvstats.web.client.sma

import dev.drzepka.pvstats.model.device.sma.SMADashValues
import dev.drzepka.pvstats.model.device.sma.SMAMeasurement
import java.net.URI

interface SMAApiClient {
    fun getDashLogger(uri: URI): SMAMeasurement
    fun getDashValues(uri: URI): SMADashValues
}