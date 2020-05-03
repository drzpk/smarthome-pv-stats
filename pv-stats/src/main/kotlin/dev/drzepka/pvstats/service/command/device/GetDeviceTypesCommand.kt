package dev.drzepka.pvstats.service.command.device

import dev.drzepka.pvstats.service.DeviceService
import dev.drzepka.pvstats.service.command.Argument
import dev.drzepka.pvstats.service.command.Command
import org.springframework.stereotype.Component

@Component
class GetDeviceTypesCommand(private val deviceService: DeviceService) : Command {
    override val name = "types"
    override val description = "return available device types"
    override val group = DeviceGroup


    override fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String> {
        val types = deviceService.getDeviceTypes()
        val list = ArrayList<String>(types.size + 2)

        list.add("")
        list.add("Available device types:")
        types.forEachIndexed { index, type ->
            list.add("${index + 1}. $type")
        }

        return list.toTypedArray()
    }

    override fun getArguments(): List<Argument> = emptyList()
}
