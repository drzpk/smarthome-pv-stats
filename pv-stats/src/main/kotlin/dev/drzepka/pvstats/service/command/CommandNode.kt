package dev.drzepka.pvstats.service.command

class CommandNode(val group: CommandGroup?) {

    private val commands = HashMap<String, Command>()
    private val childNodes = HashMap<String, CommandNode>()

    fun getCommands(): List<Command> = commands.values.sortedBy { it.name }

    fun getCommand(name: String): Command? = commands[name]

    fun addCommand(command: Command) {
        guardForDuplicateName(command.name)
        commands[command.name] = command
    }

    fun getNodes(): List<CommandNode> = childNodes.values.sortedBy { it.group!!.name }

    fun getNode(name: String): CommandNode? = childNodes[name]

    fun addNode(node: CommandNode) {
        val group = node.group!!.name
        guardForDuplicateName(group)
        childNodes[group] = node
    }

    private fun guardForDuplicateName(name: String) {
        if (commands.containsKey(name))
            throw IllegalArgumentException("Command $name already exists in this node")
        if (childNodes.containsKey(name))
            throw IllegalArgumentException("Child node with name $name already exists in this node")
    }
}