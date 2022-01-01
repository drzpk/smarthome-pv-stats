package dev.drzepka.pvstats.util

import java.text.Normalizer
import kotlin.math.min

object DataSourceUtils {
    private const val SCHEMA_PREFIX = "data_"
    private const val USER_PREFIX = "viewer_"
    private const val MAX_SQL_SCHEMA_NAME_LENGTH = 64
    private const val MAX_SQL_USER_NAME_LENGTH = 32

    private val illegalCharRegex = Regex("[^a-z0-9_]+")

    fun generateSchemaName(deviceName: String): String = normalize(SCHEMA_PREFIX + deviceName, MAX_SQL_SCHEMA_NAME_LENGTH)

    fun generateUserName(deviceName: String): String = normalize(USER_PREFIX + deviceName, MAX_SQL_USER_NAME_LENGTH)

    private fun normalize(input: String, maxLength: Int): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .lowercase()
                .replace(" ", "_")
                .replace(illegalCharRegex, "")

        return normalized.substring(0, min(normalized.length, maxLength))
    }
}

