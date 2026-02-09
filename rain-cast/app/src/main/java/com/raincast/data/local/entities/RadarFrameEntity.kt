package com.raincast.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "radar_frames")
data class RadarFrameEntity(
    @PrimaryKey val timestamp: Long,
    val path: String,
    val host: String,
    val fetchedAt: Long = System.currentTimeMillis()
)
