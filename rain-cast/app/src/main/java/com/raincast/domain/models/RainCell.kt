package com.raincast.domain.models

data class RainCell(
    val centerLat: Double,
    val centerLon: Double,
    val intensity: RainIntensity,
    val pixelCount: Int,
    val averageAlpha: Int
)

enum class RainIntensity(val label: String, val minMmPerHr: Double, val maxMmPerHr: Double) {
    LIGHT("Light", 0.5, 2.0),
    MODERATE("Moderate", 2.0, 5.0),
    HEAVY("Heavy", 5.0, 10.0),
    VERY_HEAVY("Very Heavy", 10.0, 30.0),
    EXTREME("Extreme", 30.0, Double.MAX_VALUE);

    companion object {
        fun fromAlpha(alpha: Int): RainIntensity {
            return when {
                alpha < 64 -> LIGHT
                alpha < 128 -> MODERATE
                alpha < 180 -> HEAVY
                alpha < 220 -> VERY_HEAVY
                else -> EXTREME
            }
        }

        fun fromMmPerHr(mm: Double): RainIntensity {
            return when {
                mm < 0.5 -> LIGHT
                mm < 2.0 -> LIGHT
                mm < 5.0 -> MODERATE
                mm < 10.0 -> HEAVY
                mm < 30.0 -> VERY_HEAVY
                else -> EXTREME
            }
        }
    }
}
