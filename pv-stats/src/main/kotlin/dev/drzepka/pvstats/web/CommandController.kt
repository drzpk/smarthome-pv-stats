package dev.drzepka.pvstats.web

import dev.drzepka.pvstats.model.command.CommandRequest
import dev.drzepka.pvstats.model.command.CommandResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/command")
class CommandController {

    @PostMapping("execute")
    fun executeCommand(@RequestBody request: CommandRequest): CommandResponse {
        return CommandResponse(emptyArray())
    }
}