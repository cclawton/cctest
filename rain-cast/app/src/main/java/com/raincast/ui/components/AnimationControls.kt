package com.raincast.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnimationControls(
    isPlaying: Boolean,
    currentFrameIndex: Int,
    totalFrames: Int,
    currentTimestamp: Long,
    onPlayPause: () -> Unit,
    onStepForward: () -> Unit,
    onStepBackward: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = if (currentTimestamp > 0) {
        dateFormat.format(Date(currentTimestamp * 1000))
    } else ""

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timestamp display
            Text(
                text = timeStr,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Progress indicator
            LinearProgressIndicator(
                progress = { if (totalFrames > 0) (currentFrameIndex + 1).toFloat() / totalFrames else 0f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onStepBackward) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous frame",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }

                IconButton(onClick = onStepForward) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next frame",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${currentFrameIndex + 1}/$totalFrames",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
