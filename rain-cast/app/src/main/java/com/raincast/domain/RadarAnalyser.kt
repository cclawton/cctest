package com.raincast.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.raincast.domain.models.MovementVector
import com.raincast.domain.models.RainCell
import com.raincast.domain.models.RainIntensity
import java.net.URL
import kotlin.math.*

class RadarAnalyser {

    data class FrameAnalysis(
        val timestamp: Long,
        val rainPixels: List<RainPixel>,
        val centroidLat: Double?,
        val centroidLon: Double?,
        val totalIntensity: Long
    )

    data class RainPixel(
        val tileX: Int,
        val tileY: Int,
        val pixelX: Int,
        val pixelY: Int,
        val alpha: Int,
        val lat: Double,
        val lon: Double
    )

    companion object {
        private const val ALPHA_THRESHOLD = 10
        private const val TILE_SIZE = 256
    }

    fun analyseFrame(
        tileBitmaps: Map<Pair<Int, Int>, Bitmap>,
        zoom: Int,
        userLat: Double,
        userLon: Double,
        radiusKm: Double = 80.0,
        timestamp: Long
    ): FrameAnalysis {
        val rainPixels = mutableListOf<RainPixel>()
        var weightedLatSum = 0.0
        var weightedLonSum = 0.0
        var totalWeight = 0L

        for ((tileCoord, bitmap) in tileBitmaps) {
            val (tileX, tileY) = tileCoord
            for (px in 0 until bitmap.width step 2) {
                for (py in 0 until bitmap.height step 2) {
                    val pixel = bitmap.getPixel(px, py)
                    val alpha = Color.alpha(pixel)
                    if (alpha > ALPHA_THRESHOLD) {
                        val n = 1 shl zoom
                        val lon = (tileX.toDouble() + px.toDouble() / TILE_SIZE) / n * 360.0 - 180.0
                        val latRad = atan(sinh(PI * (1 - 2.0 * (tileY.toDouble() + py.toDouble() / TILE_SIZE) / n)))
                        val lat = Math.toDegrees(latRad)

                        val dist = TileCalculator.haversineDistance(userLat, userLon, lat, lon)
                        if (dist <= radiusKm) {
                            val rp = RainPixel(tileX, tileY, px, py, alpha, lat, lon)
                            rainPixels.add(rp)
                            weightedLatSum += lat * alpha
                            weightedLonSum += lon * alpha
                            totalWeight += alpha
                        }
                    }
                }
            }
        }

        val centroidLat = if (totalWeight > 0) weightedLatSum / totalWeight else null
        val centroidLon = if (totalWeight > 0) weightedLonSum / totalWeight else null

        return FrameAnalysis(
            timestamp = timestamp,
            rainPixels = rainPixels,
            centroidLat = centroidLat,
            centroidLon = centroidLon,
            totalIntensity = totalWeight
        )
    }

    fun calculateMovementVector(
        analyses: List<FrameAnalysis>,
        windSpeedKmh: Double? = null,
        windDirectionDeg: Double? = null
    ): MovementVector {
        val validPairs = mutableListOf<Triple<Double, Double, Double>>() // speed, direction, weight

        for (i in 1 until analyses.size) {
            val prev = analyses[i - 1]
            val curr = analyses[i]

            if (prev.centroidLat == null || curr.centroidLat == null) continue
            if (prev.centroidLon == null || curr.centroidLon == null) continue

            val dx = curr.centroidLon!! - prev.centroidLon!!
            val dy = curr.centroidLat!! - prev.centroidLat!!
            val distKm = TileCalculator.haversineDistance(
                prev.centroidLat!!, prev.centroidLon!!,
                curr.centroidLat!!, curr.centroidLon!!
            )
            val dtHours = (curr.timestamp - prev.timestamp).toDouble() / 3600.0
            if (dtHours <= 0) continue

            val speed = distKm / dtHours
            val direction = Math.toDegrees(atan2(dx, dy)).let { ((it % 360) + 360) % 360 }
            val weight = i.toDouble() // More recent pairs weighted higher

            validPairs.add(Triple(speed, direction, weight))
        }

        if (validPairs.size >= 2) {
            val totalWeight = validPairs.sumOf { it.third }
            val avgSpeed = validPairs.sumOf { it.first * it.third } / totalWeight

            // Circular mean for direction
            var sinSum = 0.0
            var cosSum = 0.0
            for ((_, dir, w) in validPairs) {
                sinSum += sin(Math.toRadians(dir)) * w
                cosSum += cos(Math.toRadians(dir)) * w
            }
            val avgDirection = Math.toDegrees(atan2(sinSum, cosSum)).let { ((it % 360) + 360) % 360 }

            // Calculate variance for confidence
            val dirVariance = validPairs.map {
                val diff = abs(it.second - avgDirection)
                min(diff, 360 - diff)
            }.let { diffs -> diffs.sumOf { it * it } / diffs.size }

            val confidence = when {
                dirVariance < 400 && validPairs.size >= 3 -> MovementVector.Confidence.HIGH
                dirVariance < 1600 -> MovementVector.Confidence.MEDIUM
                else -> MovementVector.Confidence.LOW
            }

            // Cross-validate with wind if available
            if (windDirectionDeg != null && windSpeedKmh != null) {
                val angleDiff = abs(avgDirection - windDirectionDeg).let { min(it, 360 - it) }
                if (angleDiff > 45) {
                    return MovementVector(avgSpeed, avgDirection, MovementVector.Confidence.MEDIUM)
                }
            }

            return MovementVector(avgSpeed, avgDirection, confidence)
        }

        // Fallback to wind data
        if (windSpeedKmh != null && windDirectionDeg != null) {
            return MovementVector(windSpeedKmh, windDirectionDeg, MovementVector.Confidence.LOW)
        }

        return MovementVector(0.0, 0.0, MovementVector.Confidence.LOW)
    }

    fun isRainAtLocation(
        analysis: FrameAnalysis,
        userLat: Double,
        userLon: Double,
        radiusKm: Double = 2.0
    ): Pair<Boolean, RainIntensity?> {
        val nearbyPixels = analysis.rainPixels.filter {
            TileCalculator.haversineDistance(userLat, userLon, it.lat, it.lon) <= radiusKm
        }
        if (nearbyPixels.isEmpty()) return Pair(false, null)
        val avgAlpha = nearbyPixels.map { it.alpha }.average().toInt()
        return Pair(true, RainIntensity.fromAlpha(avgAlpha))
    }

    fun findNearestRainEdge(
        analysis: FrameAnalysis,
        userLat: Double,
        userLon: Double,
        fromDirectionDeg: Double,
        searchRadiusKm: Double = 80.0
    ): Pair<Double, RainIntensity>? {
        if (analysis.rainPixels.isEmpty()) return null

        val fromRad = Math.toRadians(fromDirectionDeg)
        var nearestDist = Double.MAX_VALUE
        var nearestIntensity = RainIntensity.LIGHT

        for (pixel in analysis.rainPixels) {
            val dist = TileCalculator.haversineDistance(userLat, userLon, pixel.lat, pixel.lon)
            if (dist > searchRadiusKm) continue

            // Check if this pixel is roughly in the direction rain is coming from
            val dx = pixel.lon - userLon
            val dy = pixel.lat - userLat
            val pixelAngle = atan2(dx, dy)
            val angleDiff = abs(pixelAngle - fromRad).let { min(it, 2 * PI - it) }

            if (angleDiff < PI / 3) { // Within 60 degrees of approach direction
                if (dist < nearestDist) {
                    nearestDist = dist
                    nearestIntensity = RainIntensity.fromAlpha(pixel.alpha)
                }
            }
        }

        return if (nearestDist < Double.MAX_VALUE) Pair(nearestDist, nearestIntensity) else null
    }

    fun measureCellWidth(
        analysis: FrameAnalysis,
        movementDirectionDeg: Double,
        userLat: Double,
        userLon: Double
    ): Double {
        if (analysis.rainPixels.isEmpty()) return 0.0

        val dirRad = Math.toRadians(movementDirectionDeg)
        var minProj = Double.MAX_VALUE
        var maxProj = -Double.MAX_VALUE

        for (pixel in analysis.rainPixels) {
            val dx = (pixel.lon - userLon) * cos(Math.toRadians(userLat)) * 111.0
            val dy = (pixel.lat - userLat) * 111.0
            val projection = dx * sin(dirRad) + dy * cos(dirRad)
            minProj = min(minProj, projection)
            maxProj = max(maxProj, projection)
        }

        return if (maxProj > minProj) maxProj - minProj else 0.0
    }
}
