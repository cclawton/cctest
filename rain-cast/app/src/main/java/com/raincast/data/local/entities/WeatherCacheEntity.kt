package com.raincast.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val id: Long = 1,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double?,
    val humidity: Int?,
    val windSpeed: Double?,
    val windDirection: Double?,
    val windGusts: Double?,
    val weatherCode: Int?,
    val hourlyDataJson: String?,
    val dailyDataJson: String?,
    val fetchedAt: Long = System.currentTimeMillis()
)
