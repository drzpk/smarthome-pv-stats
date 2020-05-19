package dev.drzepka.pvstats.migration

/**
 * Migration that doesn't structure of tables, but only modifies data.
 */
interface MigrationExecutor {
    val name: String

    /**
     * Executes migration, throws exception if migration cannot be performed.
     */
    fun execute()
}