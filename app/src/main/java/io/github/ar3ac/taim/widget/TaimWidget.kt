package io.github.ar3ac.taim.widget

import android.content.Context
import android.widget.RemoteViews
import androidx.compose.runtime.remember
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.fillMaxSize
import io.github.ar3ac.taim.R
import io.github.ar3ac.taim.util.BatteryUtils

/**
 * Minimalist Jetpack Glance Home Screen Widget (Taim).
 *
 * Characteristics:
 * - 100% transparent background (no colored container).
 * - Horizontally and vertically centered single column layout.
 * - Digital 24h Time: Inter SemiBold, large font, subtle drop shadow, zero battery drain via TextClock.
 * - Extended Date: Inter Regular, small font, subtle drop shadow.
 * - Battery Row: Material Design vector icon (with bolt when charging) + numerical percentage, Inter SemiBold.
 */
class TaimWidget : GlanceAppWidget() {

    companion object {
        val KEY_BATTERY_PERCENTAGE = intPreferencesKey("battery_percentage")
        val KEY_IS_CHARGING = booleanPreferencesKey("is_charging")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // Read dynamic battery state from Glance PreferencesDataStore with sticky-intent fallback
            val prefs = currentState<Preferences>()
            val currentBattery = remember { BatteryUtils.getBatteryState(context) }

            val percentage = prefs[KEY_BATTERY_PERCENTAGE] ?: currentBattery.percentage
            val isCharging = prefs[KEY_IS_CHARGING] ?: currentBattery.isCharging

            val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                setImageViewResource(
                    R.id.battery_icon,
                    if (isCharging) R.drawable.ic_battery_charging else R.drawable.ic_battery_std
                )
                setTextViewText(R.id.battery_text, "$percentage%")
            }

            AndroidRemoteViews(
                remoteViews = remoteViews,
                modifier = GlanceModifier.fillMaxSize()
            )
        }
    }
}
