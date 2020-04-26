package dev.drzepka.pvstats.util.printer

import dev.drzepka.pvstats.service.command.Command
import dev.drzepka.pvstats.service.command.CommandNode
import dev.drzepka.pvstats.service.command.Invocable

object CommandPrinter {

    fun getHelp(node: CommandNode): Array<String> {
        val list = ArrayList<String>()
        val group = node.group

        if (group != null) {
            list.add("")
            list.add("Group: " + getHierarchy(group))
            list.add("Description: " + group.description)
            list.add("")
        }

        val groups = node.getNodes()
        if (groups.isNotEmpty()) {
            list.add("Available groups:")
            groups.forEach {
                val g = it.group!!
                list.add("  ${g.name} - ${g.description}")
            }
            list.add("")
        }

        val cmds = node.getCommands()
        if (cmds.isNotEmpty()) {
            list.add("Available commands:")
            cmds.forEach {
                list.add("  ${it.name} - ${it.description}")
            }
            list.add("")
        }

        return list.toTypedArray()
    }

    fun getHelp(command: Command): Array<String> {
        val result = ArrayList<String>()
        result.add("")
        result.add("Command: " + getHierarchy(command))
        result.add("Description: ${command.description}")
        if (command.positionalArgCount() > 0)
            result.add("Positional arguments: ${command.positionalArgsUsage()}")
        result.add("")

        if (command.getArguments().isNotEmpty()) {
            result.add("Arguments:")
            command.getArguments().forEach {
                val value = if (it.hasValue) " <value>" else ""
                val req = if (it.required) " (required)" else ""
                result.add("  --${it.name}$value - ${it.description}$req")
            }
            result.add("")
        }

        return result.toTypedArray()
    }

    private fun getHierarchy(invocable: Invocable?, first: Boolean = true): String {
        // FIXME: "device > list" should be "device list"
        if (invocable == null)
            return ""
        val suffix = if (first) "" else " > "
        return getHierarchy(invocable.group, false) + invocable.name + suffix
    }
}