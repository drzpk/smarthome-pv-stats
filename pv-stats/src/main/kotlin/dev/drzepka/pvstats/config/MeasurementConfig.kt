package dev.drzepka.pvstats.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(value = "measurement")
class MeasurementConfig {

    /**
     * Maximum interval between consecutive records before gap is considered too big and some adjustments
     * should be made in order for a chart to look good. This is required because after a long inactivity
     * new measurements will cause a spike and shadow other data. This can also occur when new device with
     * large amount of generated Wh is added - a chart would present a huge single spike.
     *
     * These include (but aren't limited to):
     * * discarting deltaWh of a record
     */
    var maxAllowedIntervalSeconds = Int.MAX_VALUE
}