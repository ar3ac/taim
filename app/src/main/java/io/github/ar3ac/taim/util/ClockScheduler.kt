package io.github.ar3ac.taim.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import io.github.ar3ac.taim.receiver.ClockAlarmReceiver

object ClockScheduler {

    const val ACTION_UPDATE_TICK = "io.github.ar3ac.taim.ACTION_UPDATE_TICK"
    private const val REQUEST_CODE_TICK = 1001

    /**
     * Schedules an exact alarm at the exact boundary of the next minute (:00 seconds).
     * Uses setExactAndAllowWhileIdle so it triggers with high precision even in Doze mode.
     */
    fun scheduleNextMinute(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, ClockAlarmReceiver::class.java).apply {
            action = ACTION_UPDATE_TICK
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_TICK,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = System.currentTimeMillis()
        var nextMinute = (now / 60000 + 1) * 60000
        if (nextMinute - now < 800) {
            nextMinute += 60000
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC,
                    nextMinute,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC,
                    nextMinute,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback for devices restricting exact alarms
            alarmManager.set(
                AlarmManager.RTC,
                nextMinute,
                pendingIntent
            )
        }
    }

    /**
     * Cancels any pending minute alarms if all widgets are removed.
     */
    fun cancelNextMinute(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ClockAlarmReceiver::class.java).apply {
            action = ACTION_UPDATE_TICK
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_TICK,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
