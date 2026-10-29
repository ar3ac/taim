package io.github.ar3ac.taim.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import io.github.ar3ac.taim.util.BatteryUtils
import io.github.ar3ac.taim.util.ClockScheduler
import io.github.ar3ac.taim.widget.TaimWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClockAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isScreenOn = powerManager?.isInteractive ?: true

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Read fresh battery state synchronously from sticky broadcast
                val currentBattery = BatteryUtils.getBatteryState(context)

                // Update Glance state and redraw all widgets
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

                // If the screen is interactive, schedule next minute alarm
                if (isScreenOn) {
                    ClockScheduler.scheduleNextMinute(context)
                } else {
                    // Screen is off: pause alarms to save 100% of background battery
                    ClockScheduler.cancelNextMinute(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
