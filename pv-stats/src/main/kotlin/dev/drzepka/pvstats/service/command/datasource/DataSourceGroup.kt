package dev.drzepka.pvstats.service.command.datasource

import dev.drzepka.pvstats.service.command.CommandGroup

object DataSourceGroup : CommandGroup {
    override val name: String = "datasource"
    override val description: String = "datasource management"
    override val group: CommandGroup? = null
}