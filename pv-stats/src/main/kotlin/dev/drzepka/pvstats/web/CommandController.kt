package dev.drzepka.pvstats.web

import dev.drzepka.pvstats.model.command.CommandRequest
import dev.drzepka.pvstats.model.command.CommandResponse
import dev.drzepka.pvstats.service.command.CommandDispatcher
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/command")
class CommandController(private val commandDispatcher: CommandDispatcher) {

    @PostMapping("execute")
    fun executeCommand(@RequestBody request: CommandRequest): CommandResponse {
        return CommandResponse(commandDispatcher.dispatchCommand(request.command))
    }
}