package io.github.ar3ac.taim.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import io.github.ar3ac.taim.receiver.BatteryReceiver
import io.github.ar3ac.taim.util.BatteryUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * GlanceAppWidgetReceiver implementation for Taim widget.
 * Bridges Android AppWidget lifecycle with Jetpack Glance.
 */
class TaimWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = TaimWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Ensure the initial battery state is cached and pushed to the widget upon placement
        val initialBattery = BatteryUtils.getBatteryState(context)
        CoroutineScope(Dispatchers.IO).launch {
            BatteryReceiver.updateAllWidgets(
                context = context,
                percentage = initialBattery.percentage,
                isCharging = initialBattery.isCharging
            )
        }
    }
}

