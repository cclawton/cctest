package com.raincast.data.api.models

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("current") val current: CurrentWeather?,
    @SerializedName("hourly") val hourly: HourlyWeather?,
    @SerializedName("daily") val daily: DailyWeather?
)

data class CurrentWeather(
    @SerializedName("time") val time: String,
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("relative_humidity_2m") val humidity: Int?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?,
    @SerializedName("wind_direction_10m") val windDirection: Double?,
    @SerializedName("wind_gusts_10m") val windGusts: Double?,
    @SerializedName("weather_code") val weatherCode: Int?
)

data class HourlyWeather(
    @SerializedName("time") val time: List<String>,
    @SerializedName("wind_speed_10m") val windSpeed: List<Double>?,
    @SerializedName("wind_direction_10m") val windDirection: List<Double>?,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int>?,
    @SerializedName("precipitation") val precipitation: List<Double>?
)

data class DailyWeather(
    @SerializedName("time") val time: List<String>?,
    @SerializedName("temperature_2m_max") val temperatureMax: List<Double>?,
    @SerializedName("temperature_2m_min") val temperatureMin: List<Double>?,
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: List<Int>?,
    @SerializedName("precipitation_sum") val precipitationSum: List<Double>?
)
