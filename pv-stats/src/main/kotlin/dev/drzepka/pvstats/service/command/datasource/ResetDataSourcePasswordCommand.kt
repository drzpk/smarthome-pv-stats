package dev.drzepka.pvstats.service.command.datasource

import dev.drzepka.pvstats.service.command.Argument
import dev.drzepka.pvstats.service.command.Command
import dev.drzepka.pvstats.service.command.CommandGroup
import dev.drzepka.pvstats.service.datasource.DataSourceService
import dev.drzepka.pvstats.util.CommandUtils
import org.springframework.stereotype.Component

@Component
class ResetDataSourcePasswordCommand(private val dataSourceService: DataSourceService) : Command {
    override val name: String = "reset"
    override val description: String = "resets data source password"
    override val group: CommandGroup? = DataSourceGroup

    override fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String> {
        val dataSourceId = positionalArguments[0].toIntOrNull() ?: return CommandUtils.error("given data source id is not an number")
        val (dataSource, password) = dataSourceService.resetPassword(dataSourceId)

        return arrayOf(
                "Password for data source${dataSource.id} has been reset",
                "",
                "New credentials for MySQL and backend connection:",
                "Username: ${dataSource.user}",
                "Password: $password",
                "",
                "Note, that password won't be displayed again, so be sure to save it"
        )
    }

    override fun getArguments(): List<Argument> = emptyList()

    override fun positionalArgCount(): Int = 1

    override fun positionalArgsUsage(): String = "<data source id>"
}