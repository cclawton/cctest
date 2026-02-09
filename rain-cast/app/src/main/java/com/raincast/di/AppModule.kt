package com.raincast.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.raincast.data.api.OpenMeteoApi
import com.raincast.data.api.RainViewerApi
import com.raincast.data.local.AppDatabase
import com.raincast.data.repository.LocationRepository
import com.raincast.data.repository.RadarRepository
import com.raincast.data.repository.WeatherRepository
import com.raincast.domain.RadarAnalyser
import com.raincast.domain.RainETAEngine
import com.raincast.location.GeocodeHelper
import com.raincast.location.LocationService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "raincast_prefs")

class AppModule(private val context: Context) {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }

    val rainViewerApi: RainViewerApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.rainviewer.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RainViewerApi::class.java)
    }

    val openMeteoApi: OpenMeteoApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoApi::class.java)
    }

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    val radarRepository: RadarRepository by lazy {
        RadarRepository(rainViewerApi, database.radarFrameDao())
    }

    val weatherRepository: WeatherRepository by lazy {
        WeatherRepository(openMeteoApi, database.weatherCacheDao())
    }

    val locationRepository: LocationRepository by lazy {
        LocationRepository(database.locationDao())
    }

    val locationService: LocationService by lazy {
        LocationService(context)
    }

    val geocodeHelper: GeocodeHelper by lazy {
        GeocodeHelper(context)
    }

    val radarAnalyser: RadarAnalyser by lazy {
        RadarAnalyser()
    }

    val rainETAEngine: RainETAEngine by lazy {
        RainETAEngine(radarAnalyser)
    }

    val dataStore: DataStore<Preferences>
        get() = context.dataStore
}
