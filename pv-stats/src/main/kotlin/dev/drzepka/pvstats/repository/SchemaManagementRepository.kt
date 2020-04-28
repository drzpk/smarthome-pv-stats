package dev.drzepka.pvstats.repository

interface SchemaManagementRepository {
    fun getSchemaName(): String
    fun getSchemaVersion(): Int
    fun createUser(user: String, password: String)
    fun dropUser(userName: String)
    fun createView(viewName: String, targetTableName: String, deviceId: Int)
    fun getViewNames(): List<String>
    fun dropView(viewName: String)
    fun tableExists(tableName: String): Boolean
    fun grantSelectPrivilegesToTable(schema: String, table: String, user: String)
}