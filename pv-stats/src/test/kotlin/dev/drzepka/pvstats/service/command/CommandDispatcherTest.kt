package dev.drzepka.pvstats.service.command

import dev.drzepka.pvstats.model.CommandException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class CommandDispatcherTest {

    @Test
    fun `getNamedArgs - single dash`() {
        Assertions.assertThrows(CommandException::class.java) {
            checkParser("-single")
        }
    }

    @Test
    fun `getNamedArgs - unknown argument`() {
        Assertions.assertThrows(CommandException::class.java) {
            checkParser("--unknown")
        }
    }

    @Test
    fun `getNamedArgs - valid arguments`() {
        val parsed = checkParser("--valued abc --other asdf, --novalue")
        Assertions.assertTrue { parsed["valued"]?.equals("abc") == true }
        Assertions.assertTrue { parsed.containsKey("novalue") }
    }

    @Test
    fun `getNamedArgs - required`() {
        Assertions.assertThrows(CommandException::class.java) {
            checkParser("")
        }
    }

    private fun checkParser(input: String): Map<String, String?> {
        val dispatcher = CommandDispatcher(emptyList())
        return dispatcher.getNamedArgs(getKnownArgs(), input.split(Regex("\\s+")), arrayListOf())
    }

    private fun getKnownArgs(): List<Argument> {
        return listOf(
                Argument("valued", "desc", hasValue = true, required = true),
                Argument("novalue", "desc", false),
                Argument("other", "desc", true)
        )
    }

}