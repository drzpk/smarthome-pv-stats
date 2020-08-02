package dev.drzepka.pvstats.model.datasource

import dev.drzepka.pvstats.entity.DataSource

data class DataSourceCredentials(
        val dataSource: DataSource,
        val plainPassword: String
)