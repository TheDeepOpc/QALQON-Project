package com.qalqon.security.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.qalqon.security.MainActivity
import com.qalqon.security.R
import com.qalqon.security.alert.AlertActivity

object NotificationHelper {
    private const val MONITOR_CHANNEL_ID = "qalqon_monitor_channel"
    private const val ALERT_CHANNEL_ID = "qalqon_alert_channel"
    const val MONITOR_NOTIFICATION_ID = 1101

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val monitorChannel = NotificationChannel(
            MONITOR_CHANNEL_ID,
            context.getString(R.string.monitor_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        val alertChannel = NotificationChannel(
            ALERT_CHANNEL_ID,
            context.getString(R.string.alert_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        )
        manager.createNotificationChannel(monitorChannel)
        manager.createNotificationChannel(alertChannel)
    }

    fun monitorNotification(context: Context): Notification {
        val intent = PendingIntent.getActivity(
            context,
            0,
            android.content.Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(context, MONITOR_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.monitor_notification_text))
            .setOngoing(true)
            .setContentIntent(intent)
            .build()
    }

    fun showRiskNotification(context: Context, eventId: Long, apkPath: String, source: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val fullScreenIntent = PendingIntent.getActivity(
            context,
            eventId.toInt(),
            AlertActivity.createIntent(context, eventId, apkPath, source),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Untrusted APK detected")
            .setContentText("Source: $source")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenIntent, true)
            .build()
        manager.notify(eventId.toInt().coerceAtLeast(2001), notification)
    }
}
