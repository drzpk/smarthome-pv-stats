package dev.drzepka.pvstats.util.printer

class ListPrinter {

    var header = ""
    val keyValueRows = ArrayList<Pair<String, String>>()

    fun appendRow(name: String, value: String) = keyValueRows.add(Pair(name, value))

    fun appendRow(name: String, value: Number) = keyValueRows.add(Pair(name, value.toString()))

    fun appendRow(name: String, value: Boolean) = keyValueRows.add(Pair(name, if (value) "Yes" else "No"))

    fun print(): Array<String> {
        val maxKeyLength = keyValueRows.maxOf { it.first.length }
        val result = Array(keyValueRows.size + 3) { "" }
        result[1] = header
        result[2] = "-".repeat(maxKeyLength + 1)

        keyValueRows.forEachIndexed { i, v ->
            result[i + 3] = v.first.padStart(maxKeyLength) + ": " + v.second
        }

        return result
    }
}