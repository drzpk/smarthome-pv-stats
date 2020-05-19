package dev.drzepka.pvstats.service

import dev.drzepka.pvstats.entity.Migration
import dev.drzepka.pvstats.migration.MigrationExecutor
import dev.drzepka.pvstats.migration.SMAPowerMigrationExecutor
import dev.drzepka.pvstats.repository.MigrationRepository
import dev.drzepka.pvstats.util.Logger
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import kotlin.reflect.KClass

/**
 * Performs data migration that due to their complexity or other reasons cannot be performed
 * as Flyway migrations.
 *
 * **Migrations must follow these rules:**
 * * migration order isn't checked - it's developer's responsibility to order them correctly in this class
 * * since we're dealing with (relatively) large datasets here, this service doesn't start any transaction because it
 * might be too slow and resource-consuming (and even impossible without adjusting DB settings).
 * Instead, each [MigrationExecutor] should do decide if it's job can have one large transaction or be divided
 * into batches
 * them fails
 */
@Service
class MigrationService(
        private val beanFactory: AutowireCapableBeanFactory,
        private val migrationRepository: MigrationRepository
) {

    private val log by Logger()

    @PostConstruct
    fun migrate() {
        log.info("Checking migrations")

        val migrations = migrationRepository.findAll()
        val failedMigration = migrations.firstOrNull { it.status.not() }
        if (failedMigration != null)
            throw IllegalStateException("Database contains failed migration: $failedMigration")

        MIGRATIONS.forEach { candidate ->
            val instance = getInstance(candidate)
            val executed = migrations.any { it.name == instance.name }
            if (!executed) {
                executeMigration(instance)
            }
        }
    }

    private fun getInstance(executorClass: KClass<out MigrationExecutor>): MigrationExecutor {
        return beanFactory.createBean(executorClass.java)
    }

    private fun executeMigration(executor: MigrationExecutor) {
        log.info("Executing migration ${executor.name}")
        var migrationException: Exception? = null
        val startTime = System.currentTimeMillis()
        val endTime: Long
        try {
            executor.execute()
        } catch (e: Exception) {
            migrationException = e
        } finally {
            endTime = System.currentTimeMillis()
        }

        val migration = Migration()
        migration.name = executor.name
        migration.executionTimeMs = (endTime - startTime).toInt()
        migration.status = migrationException == null
        migrationRepository.save(migration)

        if (migrationException != null)
            throw IllegalStateException("Migration '${executor.name} failed", migrationException)


        log.info("Migration ${executor.name} finished. Execution time: ${endTime - startTime} ms")
    }

    companion object {
        private val MIGRATIONS: List<KClass<out MigrationExecutor>> = listOf(
                SMAPowerMigrationExecutor::class
        )
    }
}