package dev.drzepka.pvstats.logger.model.config

import dev.drzepka.pvstats.logger.util.PropertiesLoader

class MainConfig private constructor(
        val logDirectory: String
) {

    companion object {
        fun loadFromProperties(loader: PropertiesLoader): MainConfig {
            return MainConfig(
                    loader.getString("log_directory")!!
            )
        }
    }
}