package io.github.ar3ac.taim.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import io.github.ar3ac.taim.model.BatteryState

/**
 * Utility functions to extract battery state synchronously using Android's sticky broadcast.
 */
object BatteryUtils {

    /**
     * Reads the current battery state without waiting for a broadcast event.
     * Android caches [Intent.ACTION_BATTERY_CHANGED] as a sticky intent.
     */
    fun getBatteryState(context: Context): BatteryState {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = context.registerReceiver(null, filter)
        return parseBatteryIntent(batteryIntent)
    }

    /**
     * Parses an [Intent.ACTION_BATTERY_CHANGED] intent into a [BatteryState].
     */
    fun parseBatteryIntent(intent: Intent?): BatteryState {
        if (intent == null) {
            return BatteryState(percentage = 100, isCharging = false)
        }

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = if (level >= 0 && scale > 0) {
            (level * 100) / scale
        } else {
            100
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        return BatteryState(
            percentage = percentage.coerceIn(0, 100),
            isCharging = isCharging
        )
    }
}

