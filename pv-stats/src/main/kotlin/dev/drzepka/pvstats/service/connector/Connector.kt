package dev.drzepka.pvstats.service.connector

import dev.drzepka.pvstats.entity.Device
import dev.drzepka.pvstats.entity.EnergyMeasurement
import dev.drzepka.pvstats.model.DeviceType

interface Connector {
    val type: DeviceType

    fun collectMeasurements(device: Device, lastMeasurement: EnergyMeasurement): List<EnergyMeasurement>
}