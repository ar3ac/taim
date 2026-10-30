package io.github.ar3ac.taim

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import io.github.ar3ac.taim.receiver.BatteryReceiver
import io.github.ar3ac.taim.util.BatteryUtils
import io.github.ar3ac.taim.util.ClockScheduler
import io.github.ar3ac.taim.widget.TaimWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaimApp : Application() {

    private val batteryReceiver = BatteryReceiver()

    // System tick and screen wake receiver
    private val timeTickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_TIME_TICK,
                Intent.ACTION_SCREEN_ON,
                Intent.ACTION_USER_PRESENT -> {
                    refreshWidgets(context)
                    ClockScheduler.scheduleNextMinute(context)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Battery state changes (sticky broadcast)
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

        // System minute tick and screen on/unlock
        val tickFilter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        ContextCompat.registerReceiver(
            this,
            timeTickReceiver,
            tickFilter,
            ContextCompat.RECEIVER_EXPORTED
        )

        // Start minute ticker
        ClockScheduler.scheduleNextMinute(this)
    }

    companion object {
        fun refreshWidgets(context: Context) {
            val currentBattery = BatteryUtils.getBatteryState(context)
            CoroutineScope(Dispatchers.IO).launch {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(TaimWidget::class.java)
                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[TaimWidget.KEY_BATTERY_PERCENTAGE] = currentBattery.percentage
                            this[TaimWidget.KEY_IS_CHARGING] = currentBattery.isCharging
                        }
                    }
                    TaimWidget().update(context, glanceId)
                }
            }
        }
    }
}
