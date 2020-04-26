package dev.drzepka.pvstats.service.command.device

import dev.drzepka.pvstats.Commons
import dev.drzepka.pvstats.service.DeviceService
import dev.drzepka.pvstats.service.command.Argument
import dev.drzepka.pvstats.service.command.Command
import dev.drzepka.pvstats.util.printer.TablePrinter
import org.springframework.stereotype.Component

@Component
class ListDevicesCommand(private val deviceService: DeviceService) : Command {
    override val name: String = "list"
    override val description: String = "lists available devices"
    override val group: DeviceGroup = DeviceGroup


    override fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String> {
        val headers = listOf("Id", "Name", "Description", "Type", "Created at", "Active")

        val data = deviceService.getDevices(active = true, inactive = namedArguments.containsKey(ARG_ALL)).map {
            val entry = ArrayList<String>(headers.size)
            entry.add(it.id.toString())
            entry.add(it.name)
            entry.add(it.description ?: "")
            entry.add(it.type.toString())
            entry.add(Commons.DATE_FORMAT.format(it.createdAt))
            entry.add(if (it.active) "Yes" else "No")
            entry
        }

        return TablePrinter.getTable(headers, data)
    }

    override fun getArguments(): List<Argument> = listOf(Argument(ARG_ALL, "get all devices (including inactive)", false))

    companion object {
        private const val ARG_ALL = "all"
    }
}