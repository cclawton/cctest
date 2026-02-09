package com.raincast.data.repository

import com.google.gson.Gson
import com.raincast.data.api.OpenMeteoApi
import com.raincast.data.api.models.OpenMeteoResponse
import com.raincast.data.local.WeatherCacheDao
import com.raincast.data.local.entities.WeatherCacheEntity

class WeatherRepository(
    private val api: OpenMeteoApi,
    private val dao: WeatherCacheDao,
    private val gson: Gson = Gson()
) {
    suspend fun fetchWeather(latitude: Double, longitude: Double): Result<OpenMeteoResponse> {
        return try {
            val response = api.getForecast(latitude, longitude)
            // Cache the response
            val entity = WeatherCacheEntity(
                latitude = latitude,
                longitude = longitude,
                temperature = response.current?.temperature,
                humidity = response.current?.humidity,
                windSpeed = response.current?.windSpeed,
                windDirection = response.current?.windDirection,
                windGusts = response.current?.windGusts,
                weatherCode = response.current?.weatherCode,
                hourlyDataJson = response.hourly?.let { gson.toJson(it) },
                dailyDataJson = response.daily?.let { gson.toJson(it) }
            )
            dao.insertWeather(entity)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCachedWeather(): WeatherCacheEntity? {
        return dao.getCachedWeather()
    }

    suspend fun isCacheStale(maxAgeMs: Long = 30 * 60 * 1000): Boolean {
        val cached = dao.getCachedWeather() ?: return true
        return System.currentTimeMillis() - cached.fetchedAt > maxAgeMs
    }
}
