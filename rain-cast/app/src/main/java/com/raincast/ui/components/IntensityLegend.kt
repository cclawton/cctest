package com.raincast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.raincast.ui.theme.*

@Composable
fun IntensityLegend(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val items = listOf(
        LegendItem("Light", "0.5-2 mm/hr", RainLight),
        LegendItem("Moderate", "2-5 mm/hr", RainModerate),
        LegendItem("Heavy", "5-10 mm/hr", RainHeavy),
        LegendItem("V.Heavy", "10-30 mm/hr", RainVeryHeavy),
        LegendItem("Extreme", "30+ mm/hr", RainExtreme)
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shadowElevation = 2.dp
    ) {
        if (compact) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(item.color)
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Rain Intensity",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                items.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp, 12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(item.color)
                        )
                        Text(
                            text = "${item.label} (${item.description})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

private data class LegendItem(
    val label: String,
    val description: String,
    val color: androidx.compose.ui.graphics.Color
)
