package dev.drzepka.pvstats.util

import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.Test

class DataSourceUtilsTest {

    @Test
    fun `should generate valid schema name`() {
        BDDAssertions.then(DataSourceUtils.generateSchemaName("nÓrmal_devicę NAME")).isEqualTo("data_normal_device_name")
        BDDAssertions.then(DataSourceUtils.generateSchemaName("x".repeat(65))).hasSize(64)
    }
}