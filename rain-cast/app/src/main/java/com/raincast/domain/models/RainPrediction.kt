package com.raincast.domain.models

data class RainPrediction(
    val isCurrentlyRaining: Boolean,
    val currentIntensity: RainIntensity?,
    val etaMinutes: Int?,
    val clearingMinutes: Int?,
    val approachIntensity: RainIntensity?,
    val estimatedDurationMinutes: Int?,
    val movementVector: MovementVector?,
    val minuteByMinute: List<MinuteIntensity>,
    val confidence: MovementVector.Confidence,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val displayMessage: String
        get() = when {
            isCurrentlyRaining && currentIntensity != null ->
                "Currently raining - ${currentIntensity.label}"
            etaMinutes != null && etaMinutes > 0 ->
                "Rain in ~$etaMinutes min"
            else -> "No rain nearby"
        }

    val subMessage: String
        get() = when {
            isCurrentlyRaining && clearingMinutes != null ->
                "Clearing in ~$clearingMinutes min"
            etaMinutes != null && approachIntensity != null && movementVector != null ->
                "${approachIntensity.label} rain from the ${movementVector.fromDirectionCompass}"
            else -> "No rain expected in the next 60 minutes"
        }
}

data class MinuteIntensity(
    val minuteFromNow: Int,
    val intensity: RainIntensity?
)
