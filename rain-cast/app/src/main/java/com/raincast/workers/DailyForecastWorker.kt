package com.raincast.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.raincast.RainCastApp
import com.raincast.di.dataStore
import com.raincast.notifications.RainNotificationManager
import com.raincast.ui.screens.PrefsKeys
import kotlinx.coroutines.flow.first

class DailyForecastWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? RainCastApp ?: return Result.failure()
        val weatherRepo = app.appModule.weatherRepository
        val locationService = app.appModule.locationService
        val notificationManager = RainNotificationManager(applicationContext)

        // Check if daily forecast is enabled
        val prefs = applicationContext.dataStore.data.first()
        val enabled = prefs[PrefsKeys.DAILY_FORECAST] ?: false
        if (!enabled) return Result.success()

        // Get location
        val location = locationService.getLastKnownLocation() ?: return Result.retry()

        // Fetch weather
        val result = weatherRepo.fetchWeather(location.latitude, location.longitude)
        result.onSuccess { response ->
            val precipChance = response.daily?.precipitationProbabilityMax?.firstOrNull() ?: 0
            val maxTemp = response.daily?.temperatureMax?.firstOrNull()
            val precipSum = response.daily?.precipitationSum?.firstOrNull() ?: 0.0

            val description = when {
                precipChance < 20 -> "Unlikely to rain today"
                precipChance < 50 -> "Some chance of rain"
                precipChance < 80 -> "Rain likely today"
                else -> "High chance of rain today"
            }

            notificationManager.showDailyForecastNotification(
                precipChance = precipChance,
                maxTemp = maxTemp,
                description = description
            )
        }

        return Result.success()
    }
}
