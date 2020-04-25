package dev.drzepka.pvstats.service.command

import dev.drzepka.pvstats.model.command.CommandException
import dev.drzepka.pvstats.util.CommandPrinter
import dev.drzepka.pvstats.util.Logger
import org.springframework.stereotype.Service

@Service
class CommandDispatcher(commands: List<Command>) {

    private val log by Logger()

    private val rootNode = CommandNode(null)

    init {
        buildCommandGraph(commands)
    }

    fun dispatchCommand(command: String): Array<String> {
        return try {
            findAndExecuteCommand(command)
        } catch (e: CommandException) {
            arrayOf("Error while executing command", e.message!!)
        } catch (e: Exception) {
            log.error("Unexpected error while executing command: $command", e)
            arrayOf("Cannot process command: unknown error")
        }
    }

    fun findAndExecuteCommand(command: String): Array<String> {
        // TODO: custom tokenizer (detect parentheses)
        val printHelp = command.trim().endsWith(" ?")
        val tokens = command.split(Regex("\\s+")).filter { it.isNotBlank() && it != "?" }

        if (tokens.isEmpty()) {
            // Empty command
            return emptyArray()
        }

        var currentNode = rootNode
        for (i in tokens.indices) {
            val token = tokens[i]
            val node = currentNode.getNode(token)
            if (node != null && i == tokens.size - 1) {
                val analyzedTokens = tokens.subList(0, i + 1).joinToString(separator = " ")
                throw CommandException("$analyzedTokens: command not found")
            } else if (node != null) {
                currentNode = node
                continue
            }

            val cmd = currentNode.getCommand(token)
            if (cmd == null) {
                val analyzedTokens = tokens.subList(0, i + 1).joinToString(separator = " ")
                throw CommandException("$analyzedTokens: command not found")
            }

            return if (!printHelp) {
                val args = if (i < tokens.size - 1) tokens.subList(i + 1, tokens.size) else emptyList()
                executeCommand(cmd, args)
            } else {
                CommandPrinter.getHelp(currentNode)
            }
        }

        // User typed group name
        return if (!printHelp)
            arrayOf("Error: command '$command` requires additional data. Type '$command ?' for more inforamtion")
        else
            CommandPrinter.getHelp(currentNode)
    }

    internal fun executeCommand(cmd: Command, args: List<String>): Array<String> {
        return cmd.execute(getNamedArgs(cmd.getArguments(), args), getPositionalArgs(cmd, args))
    }

    internal fun getNamedArgs(knownArgs: List<Argument>, args: List<String>): Map<String, String?> {
        val foundArgs = HashMap<String, String?>()
        var pos = 0
        while (pos < args.size) {
            var current = args[pos]
            if (!current.startsWith("-"))
                continue
            if (!current.matches(Regex("--\\S+")))
                throw CommandException("Unrecognized argument pattern: $current")
            current = current.replace("--", "")

            val arg = knownArgs.find { it.name.equals(current, ignoreCase = true) }
                    ?: throw CommandException("Unknown argument: $current")

            var value: String? = null
            if (arg.hasValue) {
                if (pos + 1 == args.size)
                    throw CommandException("Expected value for argument $current")
                value = args[pos + 1]
                pos += 2
            } else {
                pos += 1
            }

            foundArgs[arg.name] = value
        }

        return foundArgs
    }

    internal fun getPositionalArgs(cmd: Command, args: List<String>): List<String> {
        val positional = args.filter { !it.startsWith("-") }
        if (positional.size > cmd.positionalArgCount)
            throw CommandException("Required ${cmd.positionalArgCount} argument but got ${positional.size}")

        return positional
    }

    private fun buildCommandGraph(commands: List<Command>) {
        commands.forEach {
            val node = getCommandNode(it)
            node.addCommand(it)
        }

        log.info("Registered ${commands.size} commands")
    }

    private fun getCommandNode(invocable: Invocable, visitedGroups: Set<Invocable> = emptySet()): CommandNode {
        if (invocable.group == null)
            return rootNode

        val group = invocable.group!!
        if (visitedGroups.contains(group))
            throw IllegalStateException("Circular group dependency detected - group ${group.name} is already in the graph")

        val visited = HashSet<Invocable>(visitedGroups)
        visited.add(group)
        val parentNode = getCommandNode(group, visited)

        var thisNode = parentNode.getNode(group.name)
        if (thisNode == null) {
            thisNode = CommandNode(group)
            parentNode.addNode(thisNode)
        }

        return thisNode
    }
}