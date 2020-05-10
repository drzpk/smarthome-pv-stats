package dev.drzepka.pvstats.service

import dev.drzepka.pvstats.model.DeviceType
import dev.drzepka.pvstats.service.connector.Connector
import org.springframework.stereotype.Component

@Component
class ConnectorFactory(private val connectors: List<Connector>) {
    fun getConnector(type: DeviceType): Connector? = connectors.firstOrNull { it.type == type }
}