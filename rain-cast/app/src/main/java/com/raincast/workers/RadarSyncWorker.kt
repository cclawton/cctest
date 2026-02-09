package com.raincast.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.work.*
import com.raincast.RainCastApp
import com.raincast.di.dataStore
import com.raincast.domain.TileCalculator
import com.raincast.domain.models.RainIntensity
import com.raincast.notifications.RainNotificationManager
import com.raincast.ui.screens.PrefsKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.Calendar
import java.util.concurrent.TimeUnit

class RadarSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? RainCastApp ?: return Result.failure()
        val radarRepo = app.appModule.radarRepository
        val weatherRepo = app.appModule.weatherRepository
        val locationService = app.appModule.locationService
        val radarAnalyser = app.appModule.radarAnalyser
        val etaEngine = app.appModule.rainETAEngine
        val notificationManager = RainNotificationManager(applicationContext)

        // Check preferences
        val prefs = applicationContext.dataStore.data.first()
        val notificationsEnabled = prefs[PrefsKeys.NOTIFICATIONS_ENABLED] ?: true
        val alertLeadTime = prefs[PrefsKeys.ALERT_LEAD_TIME] ?: 30
        val minIntensity = prefs[PrefsKeys.MIN_INTENSITY] ?: 0
        val quietHoursEnabled = prefs[PrefsKeys.QUIET_HOURS_ENABLED] ?: true

        if (!notificationsEnabled) return Result.success()

        // Check quiet hours
        if (quietHoursEnabled) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val quietStart = prefs[PrefsKeys.QUIET_START] ?: 22
            val quietEnd = prefs[PrefsKeys.QUIET_END] ?: 6
            if (quietStart > quietEnd) {
                if (hour >= quietStart || hour < quietEnd) return Result.success()
            } else {
                if (hour in quietStart until quietEnd) return Result.success()
            }
        }

        // Get location
        val location = locationService.getLastKnownLocation() ?: return Result.retry()
        val lat = location.latitude
        val lon = location.longitude

        // Fetch radar data
        val radarResult = radarRepo.fetchLatestRadarData()
        if (radarResult.isFailure) return Result.retry()

        // Fetch weather data
        weatherRepo.fetchWeather(lat, lon)
        val weather = weatherRepo.getCachedWeather()

        // Run ETA calculation
        val frames = radarRepo.getRecentFrames(6)
        if (frames.isEmpty()) return Result.success()

        val analyses = withContext(Dispatchers.Default) {
            frames.map { frame ->
                val tiles = TileCalculator.getTilesInRadius(lat, lon, 80.0, 6)
                val tileBitmaps = mutableMapOf<Pair<Int, Int>, Bitmap>()
                for (tile in tiles) {
                    try {
                        val url = radarRepo.buildTileUrl(
                            host = frame.host, path = frame.path,
                            z = 6, x = tile.first, y = tile.second
                        )
                        val bitmap = withContext(Dispatchers.IO) {
                            URL(url).openStream().use { BitmapFactory.decodeStream(it) }
                        }
                        if (bitmap != null) tileBitmaps[tile] = bitmap
                    } catch (_: Exception) {}
                }
                radarAnalyser.analyseFrame(tileBitmaps, 6, lat, lon, 80.0, frame.timestamp)
            }
        }

        val prediction = etaEngine.calculatePrediction(
            analyses, lat, lon,
            weather?.windSpeed, weather?.windDirection
        )

        // Check alert conditions
        val meetsIntensity = when (minIntensity) {
            0 -> prediction.approachIntensity != null
            1 -> prediction.approachIntensity != null &&
                    prediction.approachIntensity.ordinal >= RainIntensity.MODERATE.ordinal
            2 -> prediction.approachIntensity != null &&
                    prediction.approachIntensity.ordinal >= RainIntensity.HEAVY.ordinal
            3 -> prediction.approachIntensity == RainIntensity.EXTREME
            else -> false
        }

        if (prediction.isCurrentlyRaining) {
            notificationManager.showCurrentlyRainingNotification(prediction)
        } else if (prediction.etaMinutes != null && prediction.etaMinutes <= alertLeadTime && meetsIntensity) {
            notificationManager.showRainApproachingNotification(prediction)
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "radar_sync_work"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RadarSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
