package dev.drzepka.pvstats.repository

import dev.drzepka.pvstats.entity.Migration
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MigrationRepository : CrudRepository<Migration, Int>