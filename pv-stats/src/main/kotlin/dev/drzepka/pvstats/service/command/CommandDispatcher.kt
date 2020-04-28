package dev.drzepka.pvstats.service.command

import dev.drzepka.pvstats.model.ApplicationException
import dev.drzepka.pvstats.model.CommandException
import dev.drzepka.pvstats.util.Logger
import dev.drzepka.pvstats.util.Tokenizer
import dev.drzepka.pvstats.util.printer.CommandPrinter
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
        } catch (e: ApplicationException) {
            arrayOf("Error while executing command", e.message!!)
        } catch (e: Exception) {
            log.error("Unexpected error while executing command: $command", e)
            arrayOf("Cannot process command: unknown error")
        }
    }

    fun findAndExecuteCommand(command: String): Array<String> {
        var tokens = Tokenizer.tokenize(command)
        val printHelp = tokens.isNotEmpty() && tokens.last() == "?"
        if (printHelp) tokens = tokens.dropLast(1)

        if (tokens.isEmpty() && printHelp)
            return CommandPrinter.getHelp(rootNode)

        if (tokens.isEmpty()) {
            // Empty command
            return emptyArray()
        }

        var currentNode = rootNode
        var cmd: Command?
        for (i in tokens.indices) {
            val token = tokens[i]
            val node = currentNode.getNode(token)

            cmd = if (node == null) currentNode.getCommand(token) else null
            if (node == null && cmd == null && i == tokens.size - 1) {
                val analyzedTokens = tokens.subList(0, i + 1).joinToString(separator = " ")
                throw CommandException("$analyzedTokens: command not found")
            } else if (node != null) {
                currentNode = node
                continue
            }

            if (cmd == null) {
                val analyzedTokens = tokens.subList(0, i + 1).joinToString(separator = " ")
                throw CommandException("$analyzedTokens: command not found")
            }

            return if (!printHelp) {
                val args = if (i < tokens.size - 1) tokens.subList(i + 1, tokens.size) else emptyList()
                executeCommand(cmd, args)
            } else {
                CommandPrinter.getHelp(cmd)
            }
        }

        // User typed group name
        return if (!printHelp)
            arrayOf("Error: command '$command` requires additional data. Type '$command ?' for more inforamtion")
        else
            CommandPrinter.getHelp(currentNode)
    }

    internal fun executeCommand(cmd: Command, args: List<String>): Array<String> {
        val positionalArgs = ArrayList<String>()
        val namedArgs = getNamedArgs(cmd.getArguments(), args, positionalArgs)
        return cmd.execute(namedArgs, getPositionalArgs(cmd, positionalArgs))
    }

    internal fun getNamedArgs(knownArgs: List<Argument>, args: List<String>, leftovers: MutableList<String>): Map<String, String?> {
        val foundArgs = HashMap<String, String?>()
        var pos = 0
        while (pos < args.size) {
            var current = args[pos]
            if (!current.startsWith("-")) {
                leftovers.add(current)
                pos++
                continue
            }
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

        knownArgs.filter { it.required }.forEach {
            if (!foundArgs.containsKey(it.name))
                throw CommandException("Argument '${it.name}' is required but wasn't specified")
        }

        return foundArgs
    }

    internal fun getPositionalArgs(cmd: Command, args: List<String>): List<String> {
        val positional = args.filter { !it.startsWith("-") }
        if (positional.size != cmd.positionalArgCount())
            throw CommandException("Required ${cmd.positionalArgCount()} argument but got ${positional.size}")

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