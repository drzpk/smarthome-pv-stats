package dev.drzepka.pvstats.logger.connector.base

/**
 * Types of data fetched from an inverter
 */
enum class DataType {
    /**
     * Real-time data, such as generation power or voltage. Only most recent values are returned from device.
     * This type of data can't be obtained later.
     */
    METRICS,
    /**
     * Data containing values (such as power generation) with timestamp from last few hours.
     * The difference between this type and METRICS is that in this case when logging is stopped and
     * then resumed within the certain interval, no data will be lost.
     */
    MEASUREMENT
}