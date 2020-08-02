package dev.drzepka.pvstats.service.command.datasource

import dev.drzepka.pvstats.service.command.Argument
import dev.drzepka.pvstats.service.command.Command
import dev.drzepka.pvstats.service.command.CommandGroup
import dev.drzepka.pvstats.service.datasource.DataSourceService
import dev.drzepka.pvstats.util.CommandUtils
import org.springframework.stereotype.Component

@Component
class CreateDataSourceCommand(private val dataSourceService: DataSourceService) : Command {
    override val name: String = "create"
    override val description: String = "creates new data source bound to device"
    override val group: CommandGroup? = DataSourceGroup

    override fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String> {
        val deviceId = positionalArguments[0].toIntOrNull() ?: return CommandUtils.error("given device id is not an number")
        val (dataSource, password) = dataSourceService.createDataSource(deviceId)

        return arrayOf(
                "Data source ${dataSource.id} for device ${dataSource.device!!.id} has been created",
                "",
                "Credentials for MySQL and backend connection:",
                "Database: ${dataSource.schema}",
                "Username: ${dataSource.user}",
                "Password: $password",
                "",
                "Note, that password won't be displayed again, so be sure to save it"
        )
    }

    override fun getArguments(): List<Argument> = emptyList()

    override fun positionalArgCount(): Int = 1

    override fun positionalArgsUsage(): String = "<device id>"
}