package dev.drzepka.pvstats.service.command.device

import dev.drzepka.pvstats.repository.DeviceRepository
import dev.drzepka.pvstats.service.command.Argument
import dev.drzepka.pvstats.service.command.Command
import dev.drzepka.pvstats.util.CommandUtils
import org.springframework.stereotype.Component

@Component
class DeactivateDeviceCommand(private val deviceRepository: DeviceRepository) : Command {
    override val name = "deactivate"
    override val description = "deactivate specified device"
    override val group = DeviceGroup


    override fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String> {
        val idString = positionalArguments[0]
        val id = idString.toIntOrNull() ?: return CommandUtils.error("'$idString' is not a valid ID")
        val device = deviceRepository.findById(id).orElse(null)
                ?: return CommandUtils.error("device with ID $id wasn't found")

        if (!device.active)
            return arrayOf("Device ${device.id} is already inactive")

        device.active = false
        deviceRepository.save(device)
        return arrayOf("Device ${device.id} has been deactivated")
    }

    override fun getArguments(): List<Argument> = emptyList()

    override fun positionalArgCount(): Int = 1

    override fun positionalArgsUsage(): String = "<device id>"
}
