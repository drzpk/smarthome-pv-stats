package dev.drzepka.pvstats.logger.model.config

import dev.drzepka.pvstats.common.model.vendor.VendorType
import dev.drzepka.pvstats.logger.util.PropertiesLoader

class SourceConfig internal constructor(
        val sourceName: String,
        val type: VendorType,
        val url: String,
        val sn: Int,
        val timeout: Int,
        val interval: Int
) {

    companion object {
        fun loadFromProperties(sourceName: String, loader: PropertiesLoader): SourceConfig {
            return SourceConfig(
                    sourceName,
                    VendorType.valueOf(loader.getString("source.$sourceName.type", true)!!),
                    loader.getString("source.$sourceName.url", true)!!,
                    loader.getInt("source.$sourceName.sn", true)!!,
                    loader.getInt("source.$sourceName.timeout", true)!!,
                    loader.getInt("source.$sourceName.interval", true)!!
            )
        }

        fun getAvailableNames(loader: PropertiesLoader): List<String> {
            val regex = Regex("^source\\.([a-zA-Z_-]+)\\..*$")

            val names = ArrayList<String>()
            loader.properties.keys.forEach {
                val result = regex.matchEntire(it as String)
                if (result != null)
                    names.add(result.groupValues[1].toLowerCase())
            }

            return names.distinct()
        }
    }
}