package com.raincast.data.api

import com.raincast.data.api.models.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m,wind_gusts_10m,weather_code",
        @Query("hourly") hourly: String = "wind_speed_10m,wind_direction_10m,precipitation_probability,precipitation",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,precipitation_probability_max,precipitation_sum",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 2
    ): OpenMeteoResponse
}
