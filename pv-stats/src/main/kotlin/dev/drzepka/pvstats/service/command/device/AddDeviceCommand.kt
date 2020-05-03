package dev.drzepka.pvstats.service.command.device

import dev.drzepka.pvstats.model.DeviceType
import dev.drzepka.pvstats.service.DeviceService
import dev.drzepka.pvstats.service.command.Argument
import dev.drzepka.pvstats.service.command.Command
import dev.drzepka.pvstats.util.CommandUtils
import org.springframework.stereotype.Component

@Component
class AddDeviceCommand(private val deviceService: DeviceService) : Command {
    override val name = "add"
    override val description = "adds new device"
    override val group = DeviceGroup


    override fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String> {
        val name = namedArguments.getValue(ARG_NAME)!!
        val description = namedArguments.getValue(ARG_DESCRIPTION)!!
        val apiUrl = namedArguments.getValue(ARG_API_URL)!!

        val typeString = namedArguments.getValue(ARG_TYPE)!!
        val type = try {
            DeviceType.valueOf(typeString)
        } catch (e: IllegalArgumentException) {
            return CommandUtils.error("Device type $typeString doesn't exist")
        }

        val created = deviceService.addDevice(name, description, type, apiUrl)
        return arrayOf("Device has been created", "Device ID: ${created.id}")
    }

    override fun getArguments(): List<Argument> = listOf(
            Argument(ARG_NAME, "new device name", hasValue = true, required = true),
            Argument(ARG_DESCRIPTION, "new device description", hasValue = true, required = true),
            Argument(ARG_TYPE, "device type", hasValue = true, required = true),
            Argument(ARG_API_URL, "API url", hasValue = true, required = true)
    )

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_TYPE = "type"
        private const val ARG_API_URL = "api-url"
    }
}
