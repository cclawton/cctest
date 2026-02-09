package com.raincast.domain

import kotlin.math.*

object TileCalculator {
    fun latLonToTile(lat: Double, lon: Double, zoom: Int): Pair<Int, Int> {
        val n = 1 shl zoom
        val x = ((lon + 180.0) / 360.0 * n).toInt().coerceIn(0, n - 1)
        val latRad = Math.toRadians(lat)
        val y = ((1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n).toInt().coerceIn(0, n - 1)
        return Pair(x, y)
    }

    fun tileToLatLon(x: Int, y: Int, zoom: Int): Pair<Double, Double> {
        val n = 1 shl zoom
        val lon = x.toDouble() / n * 360.0 - 180.0
        val latRad = atan(sinh(PI * (1 - 2.0 * y / n)))
        val lat = Math.toDegrees(latRad)
        return Pair(lat, lon)
    }

    fun metersPerPixel(lat: Double, zoom: Int, tileSize: Int = 256): Double {
        return 156543.03392 * cos(Math.toRadians(lat)) / (1 shl zoom)
    }

    fun pixelToKm(pixels: Double, lat: Double, zoom: Int, tileSize: Int = 256): Double {
        return pixels * metersPerPixel(lat, zoom, tileSize) / 1000.0
    }

    fun kmToPixels(km: Double, lat: Double, zoom: Int, tileSize: Int = 256): Double {
        val mpp = metersPerPixel(lat, zoom, tileSize)
        return if (mpp > 0) (km * 1000.0) / mpp else 0.0
    }

    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth's radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun getTilesInRadius(centerLat: Double, centerLon: Double, radiusKm: Double, zoom: Int): List<Pair<Int, Int>> {
        val degreesLat = radiusKm / 111.0
        val degreesLon = radiusKm / (111.0 * cos(Math.toRadians(centerLat)))

        val minLat = centerLat - degreesLat
        val maxLat = centerLat + degreesLat
        val minLon = centerLon - degreesLon
        val maxLon = centerLon + degreesLon

        val (minX, maxY) = latLonToTile(minLat, minLon, zoom)
        val (maxX, minY) = latLonToTile(maxLat, maxLon, zoom)

        val tiles = mutableListOf<Pair<Int, Int>>()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                tiles.add(Pair(x, y))
            }
        }
        return tiles
    }

    fun latLonToPixelInTile(lat: Double, lon: Double, tileX: Int, tileY: Int, zoom: Int, tileSize: Int = 256): Pair<Int, Int> {
        val n = 1 shl zoom
        val xExact = (lon + 180.0) / 360.0 * n
        val latRad = Math.toRadians(lat)
        val yExact = (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n

        val pixelX = ((xExact - tileX) * tileSize).toInt()
        val pixelY = ((yExact - tileY) * tileSize).toInt()
        return Pair(pixelX, pixelY)
    }
}
