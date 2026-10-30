package io.github.ar3ac.taim.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import io.github.ar3ac.taim.receiver.BatteryReceiver
import io.github.ar3ac.taim.util.BatteryUtils
import io.github.ar3ac.taim.util.ClockScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaimWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = TaimWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Ensure fresh battery state on widget load
        val initialBattery = BatteryUtils.getBatteryState(context)
        CoroutineScope(Dispatchers.IO).launch {
            BatteryReceiver.updateAllWidgets(
                context = context,
                percentage = initialBattery.percentage,
                isCharging = initialBattery.isCharging
            )
        }

        ClockScheduler.scheduleNextMinute(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        ClockScheduler.scheduleNextMinute(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        ClockScheduler.cancelNextMinute(context)
    }
}
