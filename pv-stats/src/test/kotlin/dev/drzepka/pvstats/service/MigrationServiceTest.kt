package dev.drzepka.pvstats.service

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import dev.drzepka.pvstats.entity.Migration
import dev.drzepka.pvstats.repository.MigrationRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.config.AutowireCapableBeanFactory

class MigrationServiceTest {

    private val beanFactory = mock<AutowireCapableBeanFactory> {}
    private val migrationRepository = mock<MigrationRepository> {
        on { findAll() } doAnswer { migrations }
    }

    private val migrations = ArrayList<Migration>()

    @Test
    fun `check failed migration detection`() {
        val migration = Migration()
        migration.status = false
        migrations.add(migration)

        Assertions.assertThrows(IllegalStateException::class.java) {
            getService().migrate()
        }
    }

    private fun getService(): MigrationService = MigrationService(beanFactory, migrationRepository)
}