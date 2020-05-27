package dev.drzepka.pvstats.web

import dev.drzepka.pvstats.common.model.PutDataRequest
import dev.drzepka.pvstats.service.data.HandlerResolverService
import dev.drzepka.pvstats.util.Logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/data")
class DataController(private val handlerResolverService: HandlerResolverService) {

    private val log by Logger()

    @PutMapping
    fun putData(@RequestBody request: PutDataRequest): ResponseEntity<Unit> {
        log.debug("Handling PUT data request for device type ${request.type}")

        if (request.type == null) {
            log.warn("No type provided for data endpoint")
            return ResponseEntity.badRequest().build()
        }

        if (request.data == null) {
            log.warn("No data provided for data endpoint")
            return ResponseEntity.badRequest().build()
        }

        val service = handlerResolverService.measurement(request.type!!)
        if (service == null) {
            log.warn("No service found for vendor type ${request.type!!}")
            return ResponseEntity.notFound().build()
        }

        service.process(request.type!!, request.data!!)
        return ResponseEntity.created(URI.create("localhost")).body(null)
    }
}