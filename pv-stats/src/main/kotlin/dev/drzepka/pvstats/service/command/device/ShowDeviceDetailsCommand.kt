package dev.drzepka.pvstats.service.command.device

import dev.drzepka.pvstats.Commons
import dev.drzepka.pvstats.service.DeviceService
import dev.drzepka.pvstats.service.command.Argument
import dev.drzepka.pvstats.service.command.Command
import dev.drzepka.pvstats.util.CommandUtils
import dev.drzepka.pvstats.util.printer.ListPrinter
import org.springframework.stereotype.Component

@Component
class ShowDeviceDetailsCommand(private val deviceService: DeviceService) : Command {
    override val name = "details"
    override val description = "shows device details"
    override val group = DeviceGroup


    override fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String> {
        val idString = positionalArguments[0]
        val id = idString.toIntOrNull() ?: return CommandUtils.error("'$idString' is not a valid ID")
        val device = deviceService.getDevice(id) ?: return CommandUtils.error("device with ID $id wasn't found")

        val printer = ListPrinter()
        printer.header = "Device details"
        printer.appendRow("ID", device.id)
        printer.appendRow("Name", device.name)
        printer.appendRow("Description", device.description ?: "")
        printer.appendRow("Type", device.type.toString())
        printer.appendRow("Created at", Commons.DATE_FORMAT.format(device.createdAt))
        printer.appendRow("Active", device.active)

        return printer.print()
    }

    override fun getArguments(): List<Argument> = emptyList()

    override fun positionalArgCount(): Int = 1

    override fun positionalArgsUsage(): String = "<device id>"
}
