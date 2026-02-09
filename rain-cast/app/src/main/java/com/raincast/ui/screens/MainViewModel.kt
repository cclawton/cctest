package com.raincast.ui.screens

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.raincast.RainCastApp
import com.raincast.data.local.entities.RadarFrameEntity
import com.raincast.domain.RadarAnalyser
import com.raincast.domain.RainETAEngine
import com.raincast.domain.TileCalculator
import com.raincast.domain.models.MinuteIntensity
import com.raincast.domain.models.MovementVector
import com.raincast.domain.models.RainPrediction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as RainCastApp
    private val radarRepo = app.appModule.radarRepository
    private val weatherRepo = app.appModule.weatherRepository
    private val locationRepo = app.appModule.locationRepository
    private val locationService = app.appModule.locationService
    private val geocodeHelper = app.appModule.geocodeHelper
    private val radarAnalyser = app.appModule.radarAnalyser
    private val etaEngine = app.appModule.rainETAEngine

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _radarFrames = MutableStateFlow<List<RadarFrameEntity>>(emptyList())
    val radarFrames: StateFlow<List<RadarFrameEntity>> = _radarFrames.asStateFlow()

    private var animationJob: Job? = null
    private var refreshJob: Job? = null

    init {
        loadInitialData()
        startPeriodicRefresh()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            fetchLocation()
            refreshRadarData()
            fetchWeatherData()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun startPeriodicRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(5 * 60 * 1000L) // 5 minutes
                refreshRadarData()
                fetchWeatherData()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            fetchLocation()
            refreshRadarData()
            fetchWeatherData()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    private suspend fun fetchLocation() {
        if (!locationService.hasLocationPermission()) return
        val location = locationService.getLastKnownLocation()
        if (location != null) {
            val name = geocodeHelper.reverseGeocode(location.latitude, location.longitude)
            _uiState.value = _uiState.value.copy(
                latitude = location.latitude,
                longitude = location.longitude,
                locationName = name ?: "%.4f, %.4f".format(location.latitude, location.longitude),
                hasLocation = true
            )
        }
    }

    private suspend fun refreshRadarData() {
        val result = radarRepo.fetchLatestRadarData()
        result.onSuccess { response ->
            val frames = radarRepo.getCachedFramesList()
            _radarFrames.value = frames
            _uiState.value = _uiState.value.copy(
                radarHost = response.host,
                currentFrameIndex = (frames.size - 1).coerceAtLeast(0),
                totalFrames = frames.size,
                lastUpdated = System.currentTimeMillis(),
                isOffline = false,
                isLoading = false,
                error = null
            )
            calculateRainETA(frames)
        }
        result.onFailure { error ->
            val cached = radarRepo.getCachedFramesList()
            if (cached.isNotEmpty()) {
                _radarFrames.value = cached
                _uiState.value = _uiState.value.copy(
                    isOffline = true,
                    isLoading = false,
                    error = "Using cached data"
                )
                calculateRainETA(cached)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to fetch radar data: ${error.message}"
                )
            }
        }
    }

    private suspend fun fetchWeatherData() {
        val state = _uiState.value
        if (!state.hasLocation) return
        val result = weatherRepo.fetchWeather(state.latitude, state.longitude)
        result.onSuccess { response ->
            _uiState.value = _uiState.value.copy(
                temperature = response.current?.temperature,
                humidity = response.current?.humidity,
                windSpeed = response.current?.windSpeed,
                windDirection = response.current?.windDirection,
                windGusts = response.current?.windGusts
            )
        }
    }

    private suspend fun calculateRainETA(frames: List<RadarFrameEntity>) {
        val state = _uiState.value
        if (!state.hasLocation || frames.isEmpty()) return

        withContext(Dispatchers.Default) {
            try {
                val analyses = frames.takeLast(6).map { frame ->
                    val tiles = TileCalculator.getTilesInRadius(
                        state.latitude, state.longitude, 80.0, 6
                    )
                    val tileBitmaps = mutableMapOf<Pair<Int, Int>, Bitmap>()
                    for (tile in tiles) {
                        try {
                            val url = radarRepo.buildTileUrl(
                                host = frame.host,
                                path = frame.path,
                                z = 6, x = tile.first, y = tile.second
                            )
                            val bitmap = withContext(Dispatchers.IO) {
                                URL(url).openStream().use {
                                    BitmapFactory.decodeStream(it)
                                }
                            }
                            if (bitmap != null) {
                                tileBitmaps[tile] = bitmap
                            }
                        } catch (_: Exception) {
                            // Skip failed tiles
                        }
                    }
                    radarAnalyser.analyseFrame(
                        tileBitmaps, 6, state.latitude, state.longitude,
                        80.0, frame.timestamp
                    )
                }

                val prediction = etaEngine.calculatePrediction(
                    analyses,
                    state.latitude,
                    state.longitude,
                    state.windSpeed,
                    state.windDirection
                )

                _uiState.value = _uiState.value.copy(prediction = prediction)
            } catch (e: Exception) {
                // Keep last prediction on error
            }
        }
    }

    // Animation controls
    fun toggleAnimation() {
        val state = _uiState.value
        if (state.isAnimating) {
            stopAnimation()
        } else {
            startAnimation()
        }
    }

    private fun startAnimation() {
        animationJob?.cancel()
        _uiState.value = _uiState.value.copy(isAnimating = true)
        animationJob = viewModelScope.launch {
            while (isActive) {
                val state = _uiState.value
                val nextIndex = (state.currentFrameIndex + 1) % state.totalFrames
                _uiState.value = state.copy(currentFrameIndex = nextIndex)
                delay(state.animationSpeedMs)
            }
        }
    }

    private fun stopAnimation() {
        animationJob?.cancel()
        _uiState.value = _uiState.value.copy(isAnimating = false)
    }

    fun stepForward() {
        stopAnimation()
        val state = _uiState.value
        val nextIndex = (state.currentFrameIndex + 1) % state.totalFrames.coerceAtLeast(1)
        _uiState.value = state.copy(currentFrameIndex = nextIndex)
    }

    fun stepBackward() {
        stopAnimation()
        val state = _uiState.value
        val total = state.totalFrames.coerceAtLeast(1)
        val prevIndex = (state.currentFrameIndex - 1 + total) % total
        _uiState.value = state.copy(currentFrameIndex = prevIndex)
    }

    fun setLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val name = geocodeHelper.reverseGeocode(latitude, longitude)
            _uiState.value = _uiState.value.copy(
                latitude = latitude,
                longitude = longitude,
                locationName = name ?: "%.4f, %.4f".format(latitude, longitude),
                hasLocation = true
            )
            refreshRadarData()
            fetchWeatherData()
        }
    }

    fun onPermissionsGranted() {
        viewModelScope.launch {
            fetchLocation()
            refreshRadarData()
            fetchWeatherData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        animationJob?.cancel()
        refreshJob?.cancel()
    }
}

data class MainUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null,
    val hasLocation: Boolean = false,
    val latitude: Double = -37.8136,
    val longitude: Double = 144.9631,
    val locationName: String = "Melbourne, VIC",
    val radarHost: String = "https://tilecache.rainviewer.com",
    val currentFrameIndex: Int = 0,
    val totalFrames: Int = 0,
    val isAnimating: Boolean = false,
    val animationSpeedMs: Long = 500L,
    val prediction: RainPrediction = RainPrediction(
        isCurrentlyRaining = false,
        currentIntensity = null,
        etaMinutes = null,
        clearingMinutes = null,
        approachIntensity = null,
        estimatedDurationMinutes = null,
        movementVector = null,
        minuteByMinute = (0..59).map { MinuteIntensity(it, null) },
        confidence = MovementVector.Confidence.LOW
    ),
    val temperature: Double? = null,
    val humidity: Int? = null,
    val windSpeed: Double? = null,
    val windDirection: Double? = null,
    val windGusts: Double? = null,
    val lastUpdated: Long = 0
)
