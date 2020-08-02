package dev.drzepka.pvstats.util

import dev.drzepka.pvstats.model.ApplicationException
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TokenizerTest {

    @Test
    fun `should correctly split standard cases`() {
        val tokens = Tokenizer.tokenize("abc def 123")
        then(tokens).hasSize(3)
        then(tokens[0]).isEqualTo("abc")
        then(tokens[1]).isEqualTo("def")
        then(tokens[2]).isEqualTo("123")
    }

    @Test
    fun `should correctly split cases with quotes`() {
        val tokens = Tokenizer.tokenize("--param \"value\" normal")
        then(tokens).hasSize(3)
        then(tokens[0]).isEqualTo("--param")
        then(tokens[1]).isEqualTo("value")
        then(tokens[2]).isEqualTo("normal")
    }

    @Test
    fun `should correctly split cases with quotes at the end`() {
        val tokens = Tokenizer.tokenize("--param \"value\"")
        then(tokens).hasSize(2)
        then(tokens[0]).isEqualTo("--param")
        then(tokens[1]).isEqualTo("value")
    }

    @Test
    fun `should detect unclosed quotes`() {
        Assertions.assertThrows(ApplicationException::class.java) {
            Tokenizer.tokenize("--param \"value")
        }
    }
}