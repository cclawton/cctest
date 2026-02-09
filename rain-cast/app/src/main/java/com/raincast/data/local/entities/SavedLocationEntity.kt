package com.raincast.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
