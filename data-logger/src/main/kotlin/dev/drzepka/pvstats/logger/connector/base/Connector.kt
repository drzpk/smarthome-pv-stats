package dev.drzepka.pvstats.logger.connector.base

import dev.drzepka.pvstats.common.model.vendor.VendorData
import dev.drzepka.pvstats.logger.model.config.SourceConfig

/**
 * Retrieves data from inverter
 */
interface Connector {
    val supportedDataTypes: List<DataType>

    fun initialize(config: SourceConfig)
    fun getData(config: SourceConfig, dataType: DataType, silent: Boolean): VendorData?
    fun getUrl(config: SourceConfig, dataType: DataType): String
}