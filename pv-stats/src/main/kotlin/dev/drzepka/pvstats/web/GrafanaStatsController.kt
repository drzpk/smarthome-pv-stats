package dev.drzepka.pvstats.web

import dev.drzepka.pvstats.model.grafana.*
import dev.drzepka.pvstats.service.StatsService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/stats")
class GrafanaStatsController(private val statsService: StatsService) {

    @GetMapping("")
    fun test() {
        // Connectcion test
    }

    @PostMapping("/search")
    fun search(): SearchResponse {
        return SearchResponse("power", "deviceName")
    }

    @PostMapping("/query")
    fun query(): QueryTableResponse {

        val table = QueryTable()
        table.columns = listOf(
                TableColumn(TableColumnType.NUMBER, "power"),
                TableColumn(TableColumnType.STRING, "deviceName"),
                TableColumn(TableColumnType.NUMBER, "generationToday"),
                TableColumn(TableColumnType.NUMBER, "inverterVoltage"),
                TableColumn(TableColumnType.NUMBER, "inverterCurrent")
        )

        val stats = statsService.getCurrentStats()
        val row = if (stats != null)
            listOf(stats.power, stats.deviceName, stats.generationToday, stats.inverterVoltage, stats.inverterCurrent)
        else
            emptyList()

        table.rows = listOf(row)
        return QueryTableResponse(table)
    }

    @CrossOrigin(allowedHeaders = ["Accept", "Content-Type"], methods = [RequestMethod.POST], origins = ["*"])
    @RequestMapping(method = [RequestMethod.OPTIONS])
    fun annotationOptions() = Unit

    @PostMapping("/annotations")
    fun annotations(): List<Any> = emptyList()
}