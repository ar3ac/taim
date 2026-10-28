package io.github.ar3ac.taim.widget

import android.content.Context
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.github.ar3ac.taim.R
import io.github.ar3ac.taim.util.BatteryUtils

/**
 * Minimalist Jetpack Glance Home Screen Widget.
 *
 * Characteristics:
 * - 100% transparent background (no container backgrounds).
 * - Single column layout horizontally centered.
 * - Digital 24h Time: 70sp SemiBold, subtle drop shadow, zero battery drain via TextClock interop.
 * - Extended Date: Regular font, subtle drop shadow.
 * - Battery Row: Material Design vector icon (with bolt when charging) + numerical percentage.
 */
class TaimWidget : GlanceAppWidget() {

    companion object {
        val KEY_BATTERY_PERCENTAGE = intPreferencesKey("battery_percentage")
        val KEY_IS_CHARGING = booleanPreferencesKey("is_charging")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetLayout(context = context)
        }
    }

    @Composable
    private fun WidgetLayout(context: Context) {
        // Read dynamic battery state from Glance PreferencesDataStore
        val prefs = currentState<Preferences>()
        val defaultBattery = remember { BatteryUtils.getBatteryState(context) }

        val percentage = prefs[KEY_BATTERY_PERCENTAGE] ?: defaultBattery.percentage
        val isCharging = prefs[KEY_IS_CHARGING] ?: defaultBattery.isCharging

        // Centered single column with 100% transparent background
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color.Transparent),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Digital 24h Time & Extended Date with subtle drop shadow
            // Using AndroidRemoteViews ensures native SystemUI minute-by-minute updates
            // without requiring continuous background wakes or battery drain.
            AndroidRemoteViews(
                remoteViews = RemoteViews(context.packageName, R.layout.widget_clock_section)
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // 2. Battery status row: Material Design icon + percentage text
            BatteryRow(
                percentage = percentage,
                isCharging = isCharging
            )
        }
    }

    @Composable
    private fun BatteryRow(
        percentage: Int,
        isCharging: Boolean
    ) {
        val iconRes = if (isCharging) {
            R.drawable.ic_battery_charging
        } else {
            R.drawable.ic_battery_std
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            // Battery icon (Material Design vector)
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = if (isCharging) "Battery Charging" else "Battery Status",
                modifier = GlanceModifier.size(17.dp)
            )

            Spacer(modifier = GlanceModifier.width(6.dp))

            // Percentage text with subtle shadow simulation for readability on light wallpapers
            Box {
                // Subtle shadow layer (1dp offset, semi-transparent black)
                Text(
                    text = "$percentage%",
                    style = TextStyle(
                        color = ColorProvider(Color(0x99, 0x00, 0x00, 0x00)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier.padding(start = 1.dp, top = 1.dp)
                )

                // Crisp white foreground text
                Text(
                    text = "$percentage%",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

