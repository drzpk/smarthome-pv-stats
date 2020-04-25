package dev.drzepka.pvstats.util

import dev.drzepka.pvstats.service.command.CommandGroup
import dev.drzepka.pvstats.service.command.CommandNode

object CommandPrinter {

    fun getHelp(node: CommandNode): Array<String> {
        val list = ArrayList<String>()
        val group = node.group!!

        list.add("")
        list.add("Group: " + getFullGroupName(group))
        list.add("Description: " + group.description)
        list.add("")

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

    private fun getFullGroupName(group: CommandGroup?, first: Boolean = true): String {
        if (group == null)
            return ""
        val suffix = if (first) "" else " > "
        return getFullGroupName(group.group) + group.name + suffix
    }
}