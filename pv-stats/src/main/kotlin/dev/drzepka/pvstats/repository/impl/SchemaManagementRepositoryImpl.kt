package dev.drzepka.pvstats.repository.impl

import dev.drzepka.pvstats.model.ApplicationException
import dev.drzepka.pvstats.repository.SchemaManagementRepository
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager

@Repository
class SchemaManagementRepositoryImpl(private val entityManager: EntityManager) : SchemaManagementRepository {

    override fun getMainSchemaName(): String {
        return entityManager.createNativeQuery("select database()").singleResult as String
    }

    override fun getMainSchemaVersion(): Int {
        return entityManager
                .createNativeQuery("select installed_rank from flyway_schema_history order by 1 desc limit 1")
                .singleResult as Int
    }

    override fun createSchema(name: String) {
        validateString(name)
        val queryString = "create schema `$name`"
        entityManager.createNativeQuery(queryString).executeUpdate()
    }

    override fun dropSchema(name: String) {
        validateString(name)
        val queryString = "drop schema $name"
        entityManager.createNativeQuery(queryString).executeUpdate()
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

    override fun grantSelectPrivilegesToSchema(user: String, schema: String) {
        validateString(user)
        validateString(schema)
        val queryString = "grant select on `$schema`.* to `$user`@`%`"
        entityManager.createNativeQuery(queryString).executeUpdate()
    }

    override fun revokeAllPrivileges(user: String) {
        validateString(user)
        val queryString = "revoke all privileges, grant option from $user"
        entityManager.createNativeQuery(queryString).executeUpdate()
    }

    override fun createView(schema: String, view: String, sourceSchema: String, sourceTable: String, deviceId: Int) {
        validateString(schema)
        validateString(view)
        validateString(sourceSchema)
        validateString(sourceTable)

        val queryString = """
            create view `$schema`.`$view` as 
            select * from `$sourceSchema`.`$sourceTable`
            where device_id = $deviceId
        """.trimIndent()

        entityManager.createNativeQuery(queryString).executeUpdate()
    }

    @Suppress("UNCHECKED_CAST")
    override fun getViewNames(schema: String): List<String> {
        val queryString = "select TABLE_NAME " +
                "from information_schema.TABLES " +
                "where TABLE_SCHEMA = :tableSchema and TABLE_TYPE = 'VIEW'"
        val query = entityManager.createNativeQuery(queryString)
        query.setParameter("tableSchema", schema)
        return query.resultList as List<String>
    }

    override fun dropView(schema: String, view: String) {
        validateString(schema)
        validateString(view)
        val queryString = "drop view if exists $schema.`$view`"
        entityManager.createNativeQuery(queryString).executeUpdate()
    }

    override fun tableExists(schema: String, table: String): Boolean {
        val queryString = "select x.count = 1\n" +
                "        from (\n" +
                "            select count(*) count\n" +
                "            from information_schema.TABLES\n" +
                "            where TABLE_SCHEMA = :schemaName and TABLE_NAME = :tableName" +
                "        ) x"

        val query = entityManager.createNativeQuery(queryString)
        query.setParameter("schemaName", schema)
        query.setParameter("tableName", table)
        return (query.singleResult as Int) == 1
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