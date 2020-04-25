package dev.drzepka.pvstats.model.command

class CommandRequest {
    var command = ""
}

data class CommandResponse(var content: Array<String>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandResponse

        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        return content.contentHashCode()
    }
}