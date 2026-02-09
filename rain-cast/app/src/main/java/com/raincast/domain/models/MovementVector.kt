package com.raincast.domain.models

data class MovementVector(
    val speedKmh: Double,
    val directionDegrees: Double,
    val confidence: Confidence
) {
    enum class Confidence { HIGH, MEDIUM, LOW }

    val directionCompass: String
        get() {
            val normalized = ((directionDegrees % 360) + 360) % 360
            return when {
                normalized < 22.5 -> "N"
                normalized < 67.5 -> "NE"
                normalized < 112.5 -> "E"
                normalized < 157.5 -> "SE"
                normalized < 202.5 -> "S"
                normalized < 247.5 -> "SW"
                normalized < 292.5 -> "W"
                normalized < 337.5 -> "NW"
                else -> "N"
            }
        }

    val fromDirectionCompass: String
        get() {
            val opposite = (directionDegrees + 180) % 360
            val normalized = ((opposite % 360) + 360) % 360
            return when {
                normalized < 22.5 -> "N"
                normalized < 67.5 -> "NE"
                normalized < 112.5 -> "E"
                normalized < 157.5 -> "SE"
                normalized < 202.5 -> "S"
                normalized < 247.5 -> "SW"
                normalized < 292.5 -> "W"
                normalized < 337.5 -> "NW"
                else -> "N"
            }
        }
}
