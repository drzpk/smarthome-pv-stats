package dev.drzepka.pvstats.service.command.device

import dev.drzepka.pvstats.service.command.CommandGroup

object DeviceGroup : CommandGroup {

    override val name: String = "device"
    override val description: String = "device management"
    override val group: CommandGroup? = null

}