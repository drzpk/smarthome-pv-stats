package dev.drzepka.pvstats.util

import dev.drzepka.pvstats.model.ApplicationException

object Tokenizer {

    fun tokenize(input: String): List<String> {
        val tokens = ArrayList<String>()
        var i = 0
        var buffer = ""
        while (i < input.length) {
            val c = input[i]

            if (c == '"' || c == '\'') {
                val pos = getClosingQuotePosition(input, i)
                tokens.add(input.substring(i + 1, pos))
                i = pos + 1
            } else if (c != ' ' && c != '\t') {
                buffer += c
                i++
            } else {
                i++
                if (buffer.isNotEmpty()) {
                    tokens.add(buffer)
                    buffer = ""
                }
            }
        }

        if (buffer.isNotEmpty())
            tokens.add(buffer)

        return tokens
    }

    private fun getClosingQuotePosition(input: String, openinigQuotePos: Int): Int {
        if (openinigQuotePos == input.length - 1) throwUnclosedQuoteException(openinigQuotePos)

        val quoteChar = input[openinigQuotePos]
        val closePos = input.indexOf(quoteChar, openinigQuotePos + 1)
        if (closePos == -1) throwUnclosedQuoteException(openinigQuotePos)

        return closePos
    }

    private fun throwUnclosedQuoteException(at: Int): Nothing = throw ApplicationException("Quote at $at isn't closed")
}