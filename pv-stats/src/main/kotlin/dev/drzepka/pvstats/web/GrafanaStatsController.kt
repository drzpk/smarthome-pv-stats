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
        return SearchResponse("power", "deviceName", "generationToday", "inverterVoltage", "inverterCurrent")
    }

    @PostMapping("/query")
    fun query(@RequestBody request: QueryRequest): QueryTableResponse {
        val stats = statsService.getCurrentStats()
        val columns = if (stats != null) {
            val requested = request.targets.map { it.target }
            val availableMetrics = listOf(
                    Pair(TableColumn(TableColumnType.NUMBER, "power"), stats.power),
                    Pair(TableColumn(TableColumnType.STRING, "deviceName"), stats.deviceName),
                    Pair(TableColumn(TableColumnType.NUMBER, "generationToday"), stats.generationToday),
                    Pair(TableColumn(TableColumnType.NUMBER, "inverterVoltage"), stats.inverterVoltage),
                    Pair(TableColumn(TableColumnType.NUMBER, "inverterCurrent"), stats.inverterCurrent)
            )

            requested.map {
                val metric = availableMetrics.firstOrNull { m -> m.first.text == it }
                metric
            }
        } else {
            emptyList()
        }


        val table = QueryTable()
        table.columns = columns.map { it!!.first }
        val row = columns.map { it!!.second }

        table.rows = listOf(row)
        return QueryTableResponse(table)
    }

    @CrossOrigin(allowedHeaders = ["Accept", "Content-Type"], methods = [RequestMethod.POST], origins = ["*"])
    @RequestMapping(method = [RequestMethod.OPTIONS])
    fun annotationOptions() = Unit

    @PostMapping("/annotations")
    fun annotations(): List<Any> = emptyList()
}