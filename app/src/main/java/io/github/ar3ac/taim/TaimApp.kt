package io.github.ar3ac.taim

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import io.github.ar3ac.taim.receiver.BatteryReceiver

/**
 * Application class for Taim.
 * Dynamically registers the BatteryReceiver for ACTION_BATTERY_CHANGED
 * (which requires dynamic registration on Android 8.0+) to keep the widget synchronized.
 */
class TaimApp : Application() {

    private val batteryReceiver = BatteryReceiver()

    override fun onCreate() {
        super.onCreate()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        // On Android 14+ (API 34+), ContextCompat.RECEIVER_EXPORTED is required for system broadcasts
        ContextCompat.registerReceiver(
            this,
            batteryReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }
}

