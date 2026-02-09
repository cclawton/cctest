package com.raincast.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val RAIN_ALERTS_ID = "rain_alerts"
    const val DAILY_FORECAST_ID = "daily_forecast"
    const val MONITOR_SERVICE_ID = "monitor_service"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val rainAlerts = NotificationChannel(
            RAIN_ALERTS_ID,
            "Rain Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for approaching rain"
            enableVibration(true)
            setShowBadge(true)
        }

        val dailyForecast = NotificationChannel(
            DAILY_FORECAST_ID,
            "Daily Forecast",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Morning rain outlook summary"
        }

        val monitorService = NotificationChannel(
            MONITOR_SERVICE_ID,
            "Rain Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background rain monitoring service"
            setShowBadge(false)
        }

        manager.createNotificationChannels(listOf(rainAlerts, dailyForecast, monitorService))
    }
}
