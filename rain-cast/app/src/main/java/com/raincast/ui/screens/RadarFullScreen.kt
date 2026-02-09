package com.raincast.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raincast.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarFullScreen(
    viewModel: MainViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val frames by viewModel.radarFrames.collectAsStateWithLifecycle()
    var showLegend by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Radar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showLegend = !showLegend }) {
                        Icon(Icons.Default.Layers, "Legend")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val currentFrame = if (frames.isNotEmpty() && state.currentFrameIndex < frames.size) {
                frames[state.currentFrameIndex]
            } else null

            RadarMapView(
                latitude = state.latitude,
                longitude = state.longitude,
                currentFrame = currentFrame,
                radarHost = state.radarHost,
                modifier = Modifier.fillMaxSize()
            )

            // Legend overlay
            if (showLegend) {
                IntensityLegend(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    compact = false
                )
            }

            // Wind indicator
            state.windSpeed?.let { speed ->
                state.windDirection?.let { direction ->
                    WindIndicator(
                        speedKmh = speed,
                        directionDegrees = direction,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    )
                }
            }

            // Animation controls at bottom
            if (frames.isNotEmpty()) {
                AnimationControls(
                    isPlaying = state.isAnimating,
                    currentFrameIndex = state.currentFrameIndex,
                    totalFrames = state.totalFrames,
                    currentTimestamp = currentFrame?.timestamp ?: 0L,
                    onPlayPause = { viewModel.toggleAnimation() },
                    onStepForward = { viewModel.stepForward() },
                    onStepBackward = { viewModel.stepBackward() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
