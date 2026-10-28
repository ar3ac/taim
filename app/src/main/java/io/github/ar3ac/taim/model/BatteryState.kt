package io.github.ar3ac.taim.model

/**
 * Represents the current battery status.
 *
 * @param percentage Current battery level from 0 to 100.
 * @param isCharging True if the device is plugged in and charging or full.
 */
data class BatteryState(
    val percentage: Int,
    val isCharging: Boolean
)

