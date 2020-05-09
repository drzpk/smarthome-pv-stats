package dev.drzepka.pvstats.service.command.scheduler

import dev.drzepka.pvstats.service.command.CommandGroup

object SchedulerGroup : CommandGroup {

    override val name: String = "scheduler"
    override val description: String = "manual launching of scheduler jobs"
    override val group: CommandGroup? = null

}