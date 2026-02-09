package com.raincast.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raincast.domain.models.MovementVector
import com.raincast.domain.models.RainIntensity
import com.raincast.domain.models.RainPrediction
import com.raincast.ui.theme.*

@Composable
fun RainETACard(
    prediction: RainPrediction,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            prediction.isCurrentlyRaining -> when (prediction.currentIntensity) {
                RainIntensity.EXTREME -> RainExtreme.copy(alpha = 0.15f)
                RainIntensity.VERY_HEAVY -> RainVeryHeavy.copy(alpha = 0.15f)
                RainIntensity.HEAVY -> RainHeavy.copy(alpha = 0.15f)
                else -> RainModerate.copy(alpha = 0.15f)
            }
            prediction.etaMinutes != null -> RainBlueLight.copy(alpha = 0.1f)
            else -> NoRain.copy(alpha = 0.1f)
        },
        label = "bg_color"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main ETA display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = getIntensityColor(prediction.currentIntensity ?: prediction.approachIntensity),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = prediction.displayMessage,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub message
            Text(
                text = prediction.subMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Intensity badge and wind direction
            if (prediction.movementVector != null && prediction.movementVector.speedKmh > 0.5) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Intensity badge
                    prediction.approachIntensity?.let { intensity ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = getIntensityColor(intensity).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = intensity.label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = getIntensityColor(intensity)
                            )
                        }
                    }

                    // Wind info
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Air,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${prediction.movementVector.speedKmh.toInt()} km/h ${prediction.movementVector.directionCompass}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Confidence
                    ConfidenceBadge(prediction.confidence)
                }
            }
        }
    }
}

@Composable
fun ConfidenceBadge(confidence: MovementVector.Confidence) {
    val color = when (confidence) {
        MovementVector.Confidence.HIGH -> ConfidenceHigh
        MovementVector.Confidence.MEDIUM -> ConfidenceMedium
        MovementVector.Confidence.LOW -> ConfidenceLow
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = confidence.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

fun getIntensityColor(intensity: RainIntensity?): Color {
    return when (intensity) {
        RainIntensity.LIGHT -> RainLight
        RainIntensity.MODERATE -> RainModerate
        RainIntensity.HEAVY -> RainHeavy
        RainIntensity.VERY_HEAVY -> RainVeryHeavy
        RainIntensity.EXTREME -> RainExtreme
        null -> NoRain
    }
}
