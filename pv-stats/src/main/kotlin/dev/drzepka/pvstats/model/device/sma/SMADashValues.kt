package dev.drzepka.pvstats.model.device.sma

import org.omg.CORBA.Object

@Suppress("CAST_NEVER_SUCCEEDS", "UNCHECKED_CAST")
open class SMADashValues : HashMap<String, Any>() {

    open fun getPower(): Int { // SI unit (Watts)
        val property = getPropertyObject(OBJECT_CURRENT_POWER)
        return property["val"] as Int
    }

    open fun getDeviceName(): String {
        val property = getPropertyObject(OBJECT_DEVICE_NAME)
        return property["val"] as String
    }

    private fun getPropertyObject(name: String): Map<String, Object> {
        val result = get("result") as Map<String, Any>
        val device = result[result.keys.first()] as Map<String, Any>

        val property = device[name] as Map<String, Any>
        val firstItem = property["1"] as List<Object>
        return firstItem[0] as Map<String, Object>
    }

    companion object {
        private const val OBJECT_CURRENT_POWER = "6100_40263F00"
        private const val OBJECT_DEVICE_NAME = "6800_10821E00"
    }
}