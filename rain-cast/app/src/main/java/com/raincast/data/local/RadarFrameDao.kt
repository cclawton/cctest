package com.raincast.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.raincast.data.local.entities.RadarFrameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RadarFrameDao {
    @Query("SELECT * FROM radar_frames ORDER BY timestamp DESC")
    fun getAllFrames(): Flow<List<RadarFrameEntity>>

    @Query("SELECT * FROM radar_frames ORDER BY timestamp DESC")
    suspend fun getAllFramesList(): List<RadarFrameEntity>

    @Query("SELECT * FROM radar_frames ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentFrames(limit: Int): List<RadarFrameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFrames(frames: List<RadarFrameEntity>)

    @Query("DELETE FROM radar_frames WHERE fetchedAt < :olderThan")
    suspend fun deleteOldFrames(olderThan: Long)

    @Query("DELETE FROM radar_frames")
    suspend fun deleteAll()
}
