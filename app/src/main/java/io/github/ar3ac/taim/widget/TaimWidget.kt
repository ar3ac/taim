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
import io.github.ar3ac.taim.util.WidgetBitmapRenderer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaimWidget : GlanceAppWidget() {

    companion object {
        val KEY_BATTERY_PERCENTAGE = intPreferencesKey("battery_percentage")
        val KEY_IS_CHARGING = booleanPreferencesKey("is_charging")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val currentBattery = remember { BatteryUtils.getBatteryState(context) }

            val percentage = prefs[KEY_BATTERY_PERCENTAGE] ?: currentBattery.percentage
            val isCharging = prefs[KEY_IS_CHARGING] ?: currentBattery.isCharging

            val now = Date()
            val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            val dateString = SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(now)

            // Render high-DPI bitmaps using authentic Google Sans font
            val clockBitmap = remember(timeString) {
                WidgetBitmapRenderer.createTextBitmap(
                    context = context,
                    text = timeString,
                    fontResId = R.font.googlesans_semibold,
                    textSizeSp = 76f,
                    textColor = android.graphics.Color.WHITE
                )
            }

            val dateBitmap = remember(dateString) {
                WidgetBitmapRenderer.createTextBitmap(
                    context = context,
                    text = dateString,
                    fontResId = R.font.googlesans_regular,
                    textSizeSp = 19f,
                    textColor = android.graphics.Color.argb(230, 255, 255, 255)
                )
            }

            val batteryBitmap = remember(percentage) {
                WidgetBitmapRenderer.createTextBitmap(
                    context = context,
                    text = "$percentage%",
                    fontResId = R.font.googlesans_semibold,
                    textSizeSp = 24f,
                    textColor = android.graphics.Color.WHITE
                )
            }

            val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                setImageViewBitmap(R.id.clock_image, clockBitmap)
                setImageViewBitmap(R.id.date_image, dateBitmap)
                setImageViewBitmap(R.id.battery_text_image, batteryBitmap)
                setImageViewResource(
                    R.id.battery_icon,
                    if (isCharging) R.drawable.ic_battery_charging else R.drawable.ic_battery_std
                )
            }

            AndroidRemoteViews(
                remoteViews = remoteViews,
                modifier = GlanceModifier.fillMaxSize()
            )
        }
    }
}
