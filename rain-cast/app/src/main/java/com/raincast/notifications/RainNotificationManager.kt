package com.raincast.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.raincast.MainActivity
import com.raincast.domain.models.RainIntensity
import com.raincast.domain.models.RainPrediction

class RainNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var lastNotificationTime: Long = 0
    private val debounceMs: Long = 15 * 60 * 1000 // 15 minutes

    fun showRainApproachingNotification(prediction: RainPrediction) {
        if (!canNotify()) return
        if (System.currentTimeMillis() - lastNotificationTime < debounceMs) return

        val eta = prediction.etaMinutes ?: return
        val intensity = prediction.approachIntensity ?: return
        val direction = prediction.movementVector?.fromDirectionCompass ?: ""
        val duration = prediction.estimatedDurationMinutes

        val title = "Rain in ~$eta minutes"
        val body = buildString {
            append("${intensity.label} rain approaching")
            if (direction.isNotEmpty()) append(" from the $direction")
            if (duration != null) append(". Expected duration: ~$duration min")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.RAIN_ALERTS_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(RAIN_ALERT_ID, notification)
        lastNotificationTime = System.currentTimeMillis()
    }

    fun showCurrentlyRainingNotification(prediction: RainPrediction) {
        if (!canNotify()) return

        val intensity = prediction.currentIntensity ?: return
        val clearingMin = prediction.clearingMinutes

        val title = "Currently raining - ${intensity.label}"
        val body = if (clearingMin != null) {
            "Rain expected to clear in ~$clearingMin minutes"
        } else {
            "Rain detected at your location"
        }

        val notification = NotificationCompat.Builder(context, NotificationChannels.RAIN_ALERTS_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(RAIN_ALERT_ID, notification)
    }

    fun showDailyForecastNotification(
        precipChance: Int,
        maxTemp: Double?,
        description: String
    ) {
        if (!canNotify()) return

        val title = "Today's Rain Outlook"
        val body = buildString {
            append("$precipChance% chance of rain. ")
            append(description)
            if (maxTemp != null) append(". High of ${maxTemp.toInt()}\u00B0C")
        }

        val notification = NotificationCompat.Builder(context, NotificationChannels.DAILY_FORECAST_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(DAILY_FORECAST_NOTIFICATION_ID, notification)
    }

    fun getMonitorNotification(): android.app.Notification {
        return NotificationCompat.Builder(context, NotificationChannels.MONITOR_SERVICE_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("RainCast")
            .setContentText("Monitoring for rain")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun canNotify(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val RAIN_ALERT_ID = 1001
        const val DAILY_FORECAST_NOTIFICATION_ID = 1002
        const val MONITOR_SERVICE_NOTIFICATION_ID = 1003
    }
}
