package com.raincast.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WindIndicator(
    speedKmh: Double,
    directionDegrees: Double,
    modifier: Modifier = Modifier
) {
    val directionCompass = when {
        directionDegrees < 22.5 -> "N"
        directionDegrees < 67.5 -> "NE"
        directionDegrees < 112.5 -> "E"
        directionDegrees < 157.5 -> "SE"
        directionDegrees < 202.5 -> "S"
        directionDegrees < 247.5 -> "SW"
        directionDegrees < 292.5 -> "W"
        directionDegrees < 337.5 -> "NW"
        else -> "N"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Wind direction arrow
            val arrowColor = MaterialTheme.colorScheme.onSurfaceVariant
            Canvas(modifier = Modifier.size(24.dp)) {
                rotate(directionDegrees.toFloat(), pivot = center) {
                    val path = Path().apply {
                        moveTo(center.x, center.y - size.height * 0.4f)
                        lineTo(center.x - size.width * 0.15f, center.y + size.height * 0.2f)
                        lineTo(center.x, center.y + size.height * 0.1f)
                        lineTo(center.x + size.width * 0.15f, center.y + size.height * 0.2f)
                        close()
                    }
                    drawPath(path, arrowColor)
                }
            }

            Column {
                Text(
                    text = "${speedKmh.toInt()} km/h",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "from $directionCompass",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
