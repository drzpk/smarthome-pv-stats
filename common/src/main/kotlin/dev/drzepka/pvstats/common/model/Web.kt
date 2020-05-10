package dev.drzepka.pvstats.common.model

import dev.drzepka.pvstats.common.model.vendor.VendorType

class PutDataRequest {
    var type: VendorType? = null
    var data = "" // Base64
}