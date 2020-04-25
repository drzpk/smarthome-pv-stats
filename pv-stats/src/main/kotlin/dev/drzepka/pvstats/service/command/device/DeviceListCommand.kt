package dev.drzepka.pvstats.service.command.device

import dev.drzepka.pvstats.service.command.Argument
import dev.drzepka.pvstats.service.command.Command
import org.springframework.stereotype.Component

@Component
class DeviceListCommand : Command {
    override val name: String = "list"
    override val description: String = "lists available devices"
    override val group: DeviceGroup = DeviceGroup
    override val positionalArgCount: Int = 0


    override fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getArguments(): List<Argument> = emptyList()
}