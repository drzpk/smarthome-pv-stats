package dev.drzepka.pvstats.common.util

private const val HEX_CHARS = "0123456789abcdef"

fun hexStringToBytes(str: String): Array<Byte> {
    val bytes = Array<Byte>(str.length / 2) { 0 }
    for (i in str.indices step 2) {
        val upper = HEX_CHARS.indexOf(str[i].toLowerCase())
        val lower = HEX_CHARS.indexOf(str[i + 1].toLowerCase())
        bytes[i.shr(1)] = upper.shl(4).or(lower).toByte()
    }

    return bytes
}