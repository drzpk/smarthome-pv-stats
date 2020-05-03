package dev.drzepka.pvstats.service.command.device

import dev.drzepka.pvstats.service.DeviceService
import dev.drzepka.pvstats.service.command.Argument
import dev.drzepka.pvstats.service.command.Command
import dev.drzepka.pvstats.util.CommandUtils
import org.springframework.stereotype.Component

@Component
class ModifyDeviceCommand(private val deviceService: DeviceService) : Command {
    override val name = "modify"
    override val description = "modifies existing device"
    override val group = DeviceGroup


    override fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String> {
        val id = try {
            positionalArguments[0].toInt()
        } catch (e: NumberFormatException) {
            return CommandUtils.error("device id is not a valid number")
        }

        val name = namedArguments[ARG_NAME]
        val description = namedArguments[ARG_DESCRIPTION]
        val apiUrl = namedArguments[ARG_API_URL]

        if (name == null && description == null && apiUrl == null)
            return arrayOf("No argument were given, nothing to update")

        deviceService.modifyDevice(id, name, description, apiUrl)
        return arrayOf("Device $id has been modified")
    }

    override fun getArguments(): List<Argument> = listOf(
            Argument(ARG_NAME, "new device name", hasValue = true, required = false),
            Argument(ARG_DESCRIPTION, "new device description", hasValue = true, required = false),
            Argument(ARG_API_URL, "new API url", hasValue = true, required = false)
    )

    override fun positionalArgCount(): Int = 1

    override fun positionalArgsUsage(): String = "<device id>"

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_API_URL = "api-url"
    }
}
