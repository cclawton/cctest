package com.raincast.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.raincast.domain.models.MinuteIntensity
import com.raincast.domain.models.RainIntensity
import com.raincast.ui.theme.*

@Composable
fun RainTimeline(
    minutes: List<MinuteIntensity>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Next 60 minutes",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            minutes.forEach { minuteData ->
                TimelineBar(
                    minute = minuteData.minuteFromNow,
                    intensity = minuteData.intensity
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Now", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text("15m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text("30m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text("45m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text("60m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun TimelineBar(
    minute: Int,
    intensity: RainIntensity?
) {
    val barColor = when (intensity) {
        RainIntensity.LIGHT -> RainLight
        RainIntensity.MODERATE -> RainModerate
        RainIntensity.HEAVY -> RainHeavy
        RainIntensity.VERY_HEAVY -> RainVeryHeavy
        RainIntensity.EXTREME -> RainExtreme
        null -> MaterialTheme.colorScheme.surfaceVariant
    }

    val barHeight = when (intensity) {
        RainIntensity.LIGHT -> 12.dp
        RainIntensity.MODERATE -> 20.dp
        RainIntensity.HEAVY -> 28.dp
        RainIntensity.VERY_HEAVY -> 36.dp
        RainIntensity.EXTREME -> 44.dp
        null -> 4.dp
    }

    Box(
        modifier = Modifier.height(48.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(
            modifier = Modifier
                .width(4.dp)
                .height(barHeight)
        ) {
            drawRoundRect(
                color = barColor,
                cornerRadius = CornerRadius(2.dp.toPx()),
                size = Size(size.width, size.height)
            )
        }
    }
}
