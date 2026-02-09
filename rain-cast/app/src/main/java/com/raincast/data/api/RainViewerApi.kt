package com.raincast.data.api

import com.raincast.data.api.models.RainViewerResponse
import retrofit2.http.GET

interface RainViewerApi {
    @GET("public/weather-maps.json")
    suspend fun getWeatherMaps(): RainViewerResponse
}
