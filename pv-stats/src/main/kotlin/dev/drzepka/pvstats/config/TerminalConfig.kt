package dev.drzepka.pvstats.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("terminal")
@Component
class TerminalConfig {
    var users = emptyList<TerminalUser>()
}

class TerminalUser {
    var login = ""
    var passwordSha256 = ""
    var roles = emptyList<String>()
}