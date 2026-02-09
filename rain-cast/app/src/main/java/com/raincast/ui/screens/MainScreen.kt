package com.raincast.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raincast.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFullscreenRadar: () -> Unit = {},
    onNavigateToLocationSearch: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val frames by viewModel.radarFrames.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            viewModel.onPermissionsGranted()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.locationName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToLocationSearch) {
                        Icon(Icons.Default.Search, "Search location")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading && frames.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading radar data...", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Offline banner
                    if (state.isOffline) {
                        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val timeStr = dateFormat.format(Date(state.lastUpdated))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Offline - data from $timeStr",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Error banner
                    state.error?.let { error ->
                        if (!state.isOffline) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = error,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // Rain ETA Card
                    RainETACard(prediction = state.prediction)

                    // Minute-by-minute timeline
                    RainTimeline(minutes = state.prediction.minuteByMinute)

                    // Radar map
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        onClick = onNavigateToFullscreenRadar
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val currentFrame = if (frames.isNotEmpty() && state.currentFrameIndex < frames.size) {
                                frames[state.currentFrameIndex]
                            } else null

                            RadarMapView(
                                latitude = state.latitude,
                                longitude = state.longitude,
                                currentFrame = currentFrame,
                                radarHost = state.radarHost
                            )

                            // Legend overlay
                            IntensityLegend(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                compact = true
                            )
                        }
                    }

                    // Animation controls
                    if (frames.isNotEmpty()) {
                        val currentFrame = if (state.currentFrameIndex < frames.size) {
                            frames[state.currentFrameIndex]
                        } else null

                        AnimationControls(
                            isPlaying = state.isAnimating,
                            currentFrameIndex = state.currentFrameIndex,
                            totalFrames = state.totalFrames,
                            currentTimestamp = currentFrame?.timestamp ?: 0L,
                            onPlayPause = { viewModel.toggleAnimation() },
                            onStepForward = { viewModel.stepForward() },
                            onStepBackward = { viewModel.stepBackward() }
                        )
                    }

                    // Current conditions strip
                    CurrentConditionsStrip(state)
                }
            }
        }
    }
}

@Composable
private fun CurrentConditionsStrip(state: MainUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Temperature
            state.temperature?.let { temp ->
                ConditionItem(
                    icon = Icons.Default.Thermostat,
                    value = "%.1f\u00B0C".format(temp),
                    label = "Temp"
                )
            }

            // Wind
            state.windSpeed?.let { wind ->
                ConditionItem(
                    icon = Icons.Default.Air,
                    value = "${wind.toInt()} km/h",
                    label = "Wind"
                )
            }

            // Humidity
            state.humidity?.let { humidity ->
                ConditionItem(
                    icon = Icons.Default.WaterDrop,
                    value = "$humidity%",
                    label = "Humidity"
                )
            }
        }
    }
}

@Composable
private fun ConditionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
