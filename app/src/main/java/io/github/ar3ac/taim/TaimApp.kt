package io.github.ar3ac.taim

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import io.github.ar3ac.taim.receiver.BatteryReceiver
import io.github.ar3ac.taim.widget.TaimWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaimApp : Application() {

    private val batteryReceiver = BatteryReceiver()

    private val timeTickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            if (action == Intent.ACTION_TIME_TICK ||
                action == Intent.ACTION_TIME_CHANGED ||
                action == Intent.ACTION_TIMEZONE_CHANGED ||
                action == Intent.ACTION_SCREEN_ON
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    val manager = GlanceAppWidgetManager(context)
                    val glanceIds = manager.getGlanceIds(TaimWidget::class.java)
                    glanceIds.forEach { glanceId ->
                        TaimWidget().update(context, glanceId)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Battery receiver
        val batteryFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        ContextCompat.registerReceiver(
            this,
            batteryReceiver,
            batteryFilter,
            ContextCompat.RECEIVER_EXPORTED
        )

        // Time tick & screen on receiver
        val timeFilter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        ContextCompat.registerReceiver(
            this,
            timeTickReceiver,
            timeFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }
}
