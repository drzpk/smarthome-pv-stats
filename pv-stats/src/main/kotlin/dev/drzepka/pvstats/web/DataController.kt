package dev.drzepka.pvstats.web

import dev.drzepka.pvstats.common.model.PutDataRequest
import dev.drzepka.pvstats.common.model.vendor.VendorData
import dev.drzepka.pvstats.service.data.DataProcessorService
import dev.drzepka.pvstats.util.Logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/data")
class DataController(private val dataServices: List<DataProcessorService<out VendorData>>) {

    private val log by Logger()

    @PutMapping
    fun putData(@RequestBody request: PutDataRequest): ResponseEntity<Unit> {
        log.debug("Handling PUT data request for device type ${request.type}")

        if (request.type == null) {
            log.warn("No type provided for data endpoint")
            return ResponseEntity.badRequest().build()
        }

        val service = dataServices.firstOrNull { it.vendorType == request.type!! }
        if (service == null) {
            log.warn("No service found for vendor type ${request.type!!}")
            return ResponseEntity.notFound().build()
        }

        service.process(request.data)
        return ResponseEntity.created(URI.create("localhost")).body(null)
    }
}