package dev.drzepka.pvstats.service.command.scheduler

import dev.drzepka.pvstats.service.command.Argument
import dev.drzepka.pvstats.service.command.Command
import dev.drzepka.pvstats.service.data.DailySummaryService
import org.springframework.stereotype.Component

@Component
class RunDailySummaryCommand(private val dailySummaryService: DailySummaryService) : Command {
    override val name = "daily"
    override val description = "run daily energy measurement summary"
    override val group = SchedulerGroup


    override fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String> {
        dailySummaryService.createSummary()
        return arrayOf("Daily summary generation has been started")
    }

    override fun getArguments(): List<Argument> = emptyList()
}
