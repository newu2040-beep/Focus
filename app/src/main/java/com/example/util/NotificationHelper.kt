package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_FOCUS = "focus_sessions_channel"
        const val CHANNEL_TASKS = "task_reminders_channel"
        const val NOTIFICATION_TIMER_ID = 1001
        const val NOTIFICATION_BREAK_ID = 1002
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusChannel = NotificationChannel(
                CHANNEL_FOCUS,
                "Focus Sessions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for completed focus sessions and break reminders"
                enableVibration(true)
            }

            val taskChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Task Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for scheduled tasks"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(focusChannel)
            manager?.createNotificationChannel(taskChannel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun showSessionCompletedNotification(title: String, message: String) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_FOCUS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_TIMER_ID, builder.build())
        } catch (_: SecurityException) {
            // Gracefully ignore if revoked
        }
    }
}
