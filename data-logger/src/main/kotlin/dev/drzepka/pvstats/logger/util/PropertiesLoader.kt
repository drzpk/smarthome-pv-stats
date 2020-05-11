package dev.drzepka.pvstats.logger.util

import java.io.File
import java.io.FileInputStream
import java.util.*

class PropertiesLoader {

    val properties = Properties()

    init {
        val file = File("config.properties")
        val stream = if (file.isFile)
            FileInputStream(file)
        else
            javaClass.classLoader.getResourceAsStream("default.properties")!!

        properties.load(stream)
        stream.close()
    }

    fun getString(name: String, required: Boolean = false): String? = getValue(name, required)

    fun getInt(name: String, required: Boolean = false): Int? {
        val str = getValue(name, required)
        return str?.toInt()
    }

    private fun getValue(name: String, required: Boolean): String? {
        val value = properties.getProperty(name)
        if (value == null && required)
            throw IllegalArgumentException("Property $name is required but wasn't found")
        return value
    }
}