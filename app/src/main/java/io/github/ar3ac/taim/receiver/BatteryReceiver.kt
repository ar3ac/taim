package io.github.ar3ac.taim.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import io.github.ar3ac.taim.util.BatteryUtils
import io.github.ar3ac.taim.widget.TaimWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver responsible for intercepting ACTION_BATTERY_CHANGED,
 * ACTION_POWER_CONNECTED, and ACTION_POWER_DISCONNECTED intents,
 * and dynamically updating the Glance widget state.
 */
class BatteryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BATTERY_CHANGED ||
            action == Intent.ACTION_POWER_CONNECTED ||
            action == Intent.ACTION_POWER_DISCONNECTED
        ) {
            val batteryState = BatteryUtils.parseBatteryIntent(intent)
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    updateAllWidgets(context, batteryState.percentage, batteryState.isCharging)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        /**
         * Updates the Glance preferences state for all active instances of [TaimWidget]
         * and triggers an immediate widget redraw.
         */
        suspend fun updateAllWidgets(context: Context, percentage: Int, isCharging: Boolean) {
            val glanceManager = GlanceAppWidgetManager(context)
            val glanceIds = glanceManager.getGlanceIds(TaimWidget::class.java)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[TaimWidget.KEY_BATTERY_PERCENTAGE] = percentage
                        this[TaimWidget.KEY_IS_CHARGING] = isCharging
                    }
                }
                TaimWidget().update(context, glanceId)
            }
        }
    }
}

