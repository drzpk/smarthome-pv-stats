package dev.drzepka.pvstats.service.command.datasource

import dev.drzepka.pvstats.Commons
import dev.drzepka.pvstats.service.command.Argument
import dev.drzepka.pvstats.service.command.Command
import dev.drzepka.pvstats.service.command.CommandGroup
import dev.drzepka.pvstats.service.datasource.DataSourceService
import dev.drzepka.pvstats.util.printer.TablePrinter
import org.springframework.stereotype.Component

@Component
class ListDataSourcesCommand(private val dataSourceService: DataSourceService) : Command {
    override val name: String = "list"
    override val description: String = "lists known datasources"
    override val group: CommandGroup? = DataSourceGroup

    override fun execute(namedArguments: Map<String, String?>, positionalArguments: List<String>): Array<String> {
        val headers = listOf(
                "Id",
                "Device name",
                "Device id",
                "User name",
                "Created at"
        )

        val data = dataSourceService.getDataSources().map {
            listOf(it.id.toString(), it.device!!.name, it.device!!.id.toString(), it.user, Commons.DATE_FORMAT.format(it.createdAt))
        }

        return TablePrinter.getTable(headers, data)
    }

    override fun getArguments(): List<Argument> = emptyList()
}