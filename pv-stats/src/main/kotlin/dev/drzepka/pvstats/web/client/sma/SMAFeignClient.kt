package dev.drzepka.pvstats.web.client.sma

import dev.drzepka.pvstats.model.device.sma.SMAMeasurement
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI

@FeignClient("sma-feign-client", url = "http://localhost:8080")
@Profile("!mock")
interface SMAFeignClient : SMAApiClient {

    @GetMapping("dyn/getDashLogger.json")
    override fun getDashLogger(uri: URI): SMAMeasurement
}