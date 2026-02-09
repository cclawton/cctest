package com.raincast.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.raincast.data.local.entities.WeatherCacheEntity

@Dao
interface WeatherCacheDao {
    @Query("SELECT * FROM weather_cache WHERE id = 1")
    suspend fun getCachedWeather(): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache")
    suspend fun deleteAll()
}
