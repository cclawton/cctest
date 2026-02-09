package com.raincast.notifications

import android.content.Context
import androidx.work.*
import com.raincast.workers.DailyForecastWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object DailyForecastScheduler {

    private const val DAILY_FORECAST_WORK = "daily_forecast_work"

    fun schedule(context: Context, hour: Int = 7, minute: Int = 0) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }

        val delayMs = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<DailyForecastWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DAILY_FORECAST_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_FORECAST_WORK)
    }
}
