package dev.drzepka.pvstats.repository

interface SchemaManagementRepository {
    fun getMainSchemaName(): String
    fun getMainSchemaVersion(): Int

    fun createSchema(name: String)
    fun dropSchema(name: String)

    fun createUser(user: String, password: String)
    fun dropUser(user: String)
    fun changeUserPassword(user: String, password: String)

    fun grantSelectPrivilegesToSchema(user: String, schema: String)
    fun revokeAllPrivileges(user: String)

    fun createView(schema: String, view: String, sourceSchema: String, sourceTable: String, deviceId: Int)
    fun getViewNames(schema: String): List<String>
    fun dropView(schema: String, view: String)

    fun tableExists(schema: String, table: String): Boolean
}