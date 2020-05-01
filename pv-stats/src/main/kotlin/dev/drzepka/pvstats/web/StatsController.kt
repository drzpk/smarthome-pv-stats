package dev.drzepka.pvstats.web

import dev.drzepka.pvstats.model.CurrentStatsResponse
import dev.drzepka.pvstats.service.StatsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/stats")
class StatsController(private val statsService: StatsService) {

    @GetMapping("/current")
    fun getCurrentStats(): ResponseEntity<CurrentStatsResponse> {
        val stats = statsService.getCurrentStats()
        return if (stats != null)
            ResponseEntity.ok(stats)
        else
            ResponseEntity.notFound().build()
    }
}