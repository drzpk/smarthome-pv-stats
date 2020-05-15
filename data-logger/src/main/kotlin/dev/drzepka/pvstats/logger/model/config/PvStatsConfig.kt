package dev.drzepka.pvstats.logger.model.config

import dev.drzepka.pvstats.logger.util.PropertiesLoader
import java.net.URI

class PvStatsConfig private constructor(
        val url: URI,
        val user: String,
        val password: String,
        val timeout: Int
) {

    companion object {
        fun loadFromProperties(loader: PropertiesLoader): PvStatsConfig {
            return PvStatsConfig(
                    URI.create(loader.getString("pvstats.url", true)!!),
                    loader.getString("pvstats.user")!!,
                    loader.getString("pvstats.password")!!,
                    loader.getInt("pvstats.timeout")!!
            )
        }
    }
}