package dev.drzepka.pvstats.service.command.datasource

import dev.drzepka.pvstats.service.command.Argument
import dev.drzepka.pvstats.service.command.Command
import dev.drzepka.pvstats.service.command.CommandGroup
import dev.drzepka.pvstats.service.datasource.DataSourceService
import dev.drzepka.pvstats.util.CommandUtils
import org.springframework.stereotype.Component

@Component
class DeleteDataSourceCommand(private val dataSourceService: DataSourceService) : Command {
    override val name: String = "delete"
    override val description: String = "deletes given data source"
    override val group: CommandGroup? = DataSourceGroup

    override fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String> {
        val dataSourceId = positionalArguments[0].toIntOrNull()
                ?: return CommandUtils.error("given data source id not an number")
        dataSourceService.deleteDataSource(dataSourceId)
        return arrayOf("Data source $dataSourceId has been deleted")
    }

    override fun getArguments(): List<Argument> = emptyList()

    override fun positionalArgCount(): Int = 1

    override fun positionalArgsUsage(): String = "<data source id>"
}