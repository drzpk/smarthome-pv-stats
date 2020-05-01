package dev.drzepka.pvstats.model.grafana

import com.fasterxml.jackson.annotation.JsonProperty

class QueryRequest {
    var targets = emptyList<QueryTarget>()
}

class QueryTarget {
    var target = ""
    var type = QueryType.NONE
}

enum class QueryType {
    @JsonProperty(value = "timeseries")
    TIMESERIES,
    @JsonProperty(value = "table")
    TABLE,
    NONE
}

class QueryTableResponse(vararg queryTable: QueryTable) : ArrayList<QueryTable>(queryTable.toList())

class QueryTable {
    val type = "table"
    var columns = emptyList<TableColumn>()
    var rows = emptyList<List<Any>>()
}

data class TableColumn(val type: TableColumnType, val text: String)

enum class TableColumnType {
    @JsonProperty(value = "time")
    TIME,
    @JsonProperty(value = "string")
    STRING,
    @JsonProperty(value = "number")
    NUMBER
}
