package dev.drzepka.pvstats.repository.impl

import dev.drzepka.pvstats.model.ApplicationException
import dev.drzepka.pvstats.repository.SchemaManagementRepository
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager

@Repository
class SchemaManagementRepositoryImpl(private val entityManager: EntityManager) : SchemaManagementRepository {

    override fun getSchemaName(): String {
        return entityManager.createNativeQuery("select database()").singleResult as String
    }

    override fun getSchemaVersion(): Int {
        return entityManager
                .createNativeQuery("select installed_rank from flyway_schema_history order by 1 desc limit 1")
                .singleResult as Int
    }

    override fun createUser(user: String, password: String) {
        validateString(user)
        validateString(password)
        val queryString = "create user `$user`@`%` identified by '$password'"
        entityManager.createNativeQuery(queryString).executeUpdate()
    }

    override fun dropUser(user: String) {
        validateString(user)
        val queryString = "drop user if exists `$user`"
        entityManager.createNativeQuery(queryString).executeUpdate()
    }

    override fun changeUserPassword(user: String, password: String) {
        validateString(user)
        validateString(password)
        val queryString = "alter user `$user`@`%` identified by '$password'"
        entityManager.createNativeQuery(queryString).executeUpdate()
    }

    override fun createView(viewName: String, targetTableName: String, deviceId: Int) {
        validateString(viewName)
        validateString(targetTableName)

        val queryString = """
            create view `$viewName` as 
            select * from `$targetTableName`
            where device_id = $deviceId
        """.trimIndent()

        entityManager.createNativeQuery(queryString).executeUpdate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun getViewNames(): List<String> {
        val queryString = "select TABLE_NAME from information_schema.TABLES where TABLE_SCHEMA = database() and TABLE_TYPE = 'VIEW'"
        return entityManager.createNativeQuery(queryString).resultList as List<String>
    }

    override fun dropView(viewName: String) {
        validateString(viewName)
        val queryString = "drop view if exists $viewName"
        entityManager.createNativeQuery(queryString).executeUpdate()
    }

    override fun tableExists(tableName: String): Boolean {
        val queryString = "select x.count = 1\n" +
                "        from (\n" +
                "            select count(*) count\n" +
                "            from information_schema.TABLES\n" +
                "            where TABLE_SCHEMA = database() and TABLE_NAME = :tableName" +
                "        ) x"

        val query = entityManager.createNativeQuery(queryString)
        query.setParameter("tableName", tableName)
        return (query.singleResult as Int) == 1
    }

    override fun grantSelectPrivilegesToTable(schema: String, table: String, user: String) {
        validateString(schema)
        validateString(table)
        validateString(user)

        val queryString = "grant select on `$schema`.`$table` to `$user`@`%`".trimIndent()
        entityManager.createNativeQuery(queryString).executeUpdate()
    }

    private fun validateString(input: String) {
        input.forEach {
            if (ALLOWED_CHARS.indexOf(it) == -1)
                throw ApplicationException("forbidden character: $it")
        }
    }

    companion object {
        private const val ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_"
    }
}