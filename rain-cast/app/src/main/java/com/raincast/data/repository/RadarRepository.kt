package com.raincast.data.repository

import com.raincast.data.api.RainViewerApi
import com.raincast.data.api.models.RadarFrame
import com.raincast.data.api.models.RainViewerResponse
import com.raincast.data.local.RadarFrameDao
import com.raincast.data.local.entities.RadarFrameEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RadarRepository(
    private val api: RainViewerApi,
    private val dao: RadarFrameDao
) {
    val cachedFrames: Flow<List<RadarFrameEntity>> = dao.getAllFrames()

    private var lastResponse: RainViewerResponse? = null

    fun getHost(): String = lastResponse?.host ?: "https://tilecache.rainviewer.com"

    suspend fun fetchLatestRadarData(): Result<RainViewerResponse> {
        return try {
            val response = api.getWeatherMaps()
            lastResponse = response
            val entities = response.radar.past.map { frame ->
                RadarFrameEntity(
                    timestamp = frame.time,
                    path = frame.path,
                    host = response.host
                )
            }
            dao.deleteAll()
            dao.insertFrames(entities)
            // Clean up frames older than 3 hours
            val threeHoursAgo = System.currentTimeMillis() - (3 * 60 * 60 * 1000)
            dao.deleteOldFrames(threeHoursAgo)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCachedFramesList(): List<RadarFrameEntity> {
        return dao.getAllFramesList()
    }

    suspend fun getRecentFrames(count: Int = 6): List<RadarFrameEntity> {
        return dao.getRecentFrames(count)
    }

    fun buildTileUrl(
        host: String,
        path: String,
        z: Int,
        x: Int,
        y: Int,
        size: Int = 256,
        color: Int = 2,
        smooth: Int = 1,
        snow: Int = 0
    ): String {
        return "$host$path/$size/$z/$x/$y/$color/${smooth}_${snow}.png"
    }
}
