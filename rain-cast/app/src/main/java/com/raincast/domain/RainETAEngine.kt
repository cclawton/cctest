package com.raincast.domain

import com.raincast.domain.models.MinuteIntensity
import com.raincast.domain.models.MovementVector
import com.raincast.domain.models.RainIntensity
import com.raincast.domain.models.RainPrediction
import kotlin.math.roundToInt

class RainETAEngine(
    private val analyser: RadarAnalyser = RadarAnalyser()
) {
    fun calculatePrediction(
        frameAnalyses: List<RadarAnalyser.FrameAnalysis>,
        userLat: Double,
        userLon: Double,
        windSpeedKmh: Double? = null,
        windDirectionDeg: Double? = null
    ): RainPrediction {
        if (frameAnalyses.isEmpty()) {
            return noRainPrediction()
        }

        val movementVector = analyser.calculateMovementVector(
            frameAnalyses, windSpeedKmh, windDirectionDeg
        )

        val latestAnalysis = frameAnalyses.last()
        val (isRaining, currentIntensity) = analyser.isRainAtLocation(
            latestAnalysis, userLat, userLon
        )

        if (isRaining && currentIntensity != null) {
            return calculateCurrentlyRaining(
                latestAnalysis, userLat, userLon, currentIntensity, movementVector
            )
        }

        return calculateApproachingRain(
            latestAnalysis, userLat, userLon, movementVector
        )
    }

    private fun calculateCurrentlyRaining(
        analysis: RadarAnalyser.FrameAnalysis,
        userLat: Double,
        userLon: Double,
        intensity: RainIntensity,
        vector: MovementVector
    ): RainPrediction {
        val cellWidth = analyser.measureCellWidth(analysis, vector.directionDegrees, userLat, userLon)
        val clearingMinutes = if (vector.speedKmh > 0) {
            ((cellWidth / 2) / vector.speedKmh * 60).roundToInt().coerceIn(1, 180)
        } else {
            null
        }

        val timeline = buildTimeline(
            isRaining = true,
            intensity = intensity,
            clearingMinutes = clearingMinutes
        )

        return RainPrediction(
            isCurrentlyRaining = true,
            currentIntensity = intensity,
            etaMinutes = 0,
            clearingMinutes = clearingMinutes,
            approachIntensity = intensity,
            estimatedDurationMinutes = clearingMinutes,
            movementVector = vector,
            minuteByMinute = timeline,
            confidence = vector.confidence
        )
    }

    private fun calculateApproachingRain(
        analysis: RadarAnalyser.FrameAnalysis,
        userLat: Double,
        userLon: Double,
        vector: MovementVector
    ): RainPrediction {
        if (vector.speedKmh < 0.5) {
            return noRainPrediction(vector)
        }

        val fromDirection = (vector.directionDegrees + 180) % 360
        val nearest = analyser.findNearestRainEdge(
            analysis, userLat, userLon, fromDirection
        )

        if (nearest == null) {
            return noRainPrediction(vector)
        }

        val (distanceKm, intensity) = nearest
        val etaMinutes = (distanceKm / vector.speedKmh * 60).roundToInt()

        if (etaMinutes > 120) {
            return noRainPrediction(vector)
        }

        val cellWidth = analyser.measureCellWidth(analysis, vector.directionDegrees, userLat, userLon)
        val durationMinutes = if (vector.speedKmh > 0) {
            (cellWidth / vector.speedKmh * 60).roundToInt().coerceIn(5, 180)
        } else {
            30
        }

        val timeline = buildTimeline(
            isRaining = false,
            intensity = intensity,
            etaMinutes = etaMinutes,
            durationMinutes = durationMinutes
        )

        return RainPrediction(
            isCurrentlyRaining = false,
            currentIntensity = null,
            etaMinutes = etaMinutes,
            clearingMinutes = null,
            approachIntensity = intensity,
            estimatedDurationMinutes = durationMinutes,
            movementVector = vector,
            minuteByMinute = timeline,
            confidence = vector.confidence
        )
    }

    private fun noRainPrediction(vector: MovementVector? = null): RainPrediction {
        return RainPrediction(
            isCurrentlyRaining = false,
            currentIntensity = null,
            etaMinutes = null,
            clearingMinutes = null,
            approachIntensity = null,
            estimatedDurationMinutes = null,
            movementVector = vector,
            minuteByMinute = (0..59).map { MinuteIntensity(it, null) },
            confidence = vector?.confidence ?: MovementVector.Confidence.LOW
        )
    }

    private fun buildTimeline(
        isRaining: Boolean,
        intensity: RainIntensity?,
        etaMinutes: Int? = null,
        clearingMinutes: Int? = null,
        durationMinutes: Int? = null
    ): List<MinuteIntensity> {
        return (0..59).map { minute ->
            val minuteIntensity = when {
                isRaining -> {
                    if (clearingMinutes != null && minute >= clearingMinutes) null
                    else intensity
                }
                etaMinutes != null -> {
                    val rainStart = etaMinutes
                    val rainEnd = etaMinutes + (durationMinutes ?: 30)
                    if (minute in rainStart until rainEnd) intensity
                    else null
                }
                else -> null
            }
            MinuteIntensity(minute, minuteIntensity)
        }
    }
}
