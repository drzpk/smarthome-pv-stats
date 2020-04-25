package dev.drzepka.pvstats.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/terminal")
class TerminalController {

    @GetMapping
    fun index(): String {
        return "index"
    }
}