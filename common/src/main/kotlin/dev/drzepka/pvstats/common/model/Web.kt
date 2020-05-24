package dev.drzepka.pvstats.common.model

import dev.drzepka.pvstats.common.model.vendor.DeviceType

class PutDataRequest {
    var type: DeviceType? = null
    var data: Any? = null
}