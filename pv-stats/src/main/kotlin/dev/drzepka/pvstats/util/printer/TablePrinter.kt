package dev.drzepka.pvstats.util.printer

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

object TablePrinter {

    fun getTable(headers: List<String>, data: List<List<String>>, padding: Int = 1): Array<String> {
        if (headers.isEmpty())
            return emptyArray()

        val result = ArrayList<String>(data.size + 1)
        val cellWidths = getCellWidths(headers, data, padding)
        val innerTableWidth = cellWidths.sum() + headers.size - 1

        result.add("/" + "-".repeat(innerTableWidth) + "\\")
        result.add(getTableLine(headers, cellWidths, padding, true))
        result.add("|" + "-".repeat(innerTableWidth) + "|")

        data.forEach {
            if (it.size != headers.size)
                throw IllegalArgumentException("Data line count must match header count")
            result.add(getTableLine(it, cellWidths, padding, false))
        }
        result.add("\\" + "-".repeat(innerTableWidth) + "/")

        return result.toTypedArray()
    }

    private fun getCellWidths(headers: List<String>, data: List<List<String>>, padding: Int): Array<Int> {
        val widths = headers.map { it.length + padding * 2 }.toTypedArray()
        data.forEach { row ->
            row.forEachIndexed { i, s ->
                widths[i] = max(widths[i], s.length + padding * 2)
            }
        }

        return widths
    }

    private fun getTableLine(list: List<String>, cellWidths: Array<Int>, padding: Int, center: Boolean): String {
        val builder = StringBuilder()
        builder.append("|")

        var i = 0
        list.forEach {
            val cellWidth = cellWidths[i++]
            if (center) {
                val diff = cellWidth - it.length
                builder.append(" ".repeat(floor(diff / 2f).toInt()) + it + " ".repeat(ceil(diff / 2f).toInt()))
            } else {
                builder.append(" ".repeat(padding) + it)
                val left = cellWidth - it.length - padding
                builder.append(" ".repeat(left))
            }
            builder.append("|")
        }

        return builder.toString()
    }
}