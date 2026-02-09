package com.raincast.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.raincast.data.local.entities.SavedLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM saved_locations ORDER BY createdAt DESC")
    fun getAllLocations(): Flow<List<SavedLocationEntity>>

    @Query("SELECT * FROM saved_locations ORDER BY createdAt DESC")
    suspend fun getAllLocationsList(): List<SavedLocationEntity>

    @Query("SELECT * FROM saved_locations WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveLocation(): SavedLocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocationEntity): Long

    @Update
    suspend fun updateLocation(location: SavedLocationEntity)

    @Delete
    suspend fun deleteLocation(location: SavedLocationEntity)

    @Query("UPDATE saved_locations SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE saved_locations SET isActive = 1 WHERE id = :locationId")
    suspend fun activateLocation(locationId: Long)

    @Query("SELECT COUNT(*) FROM saved_locations")
    suspend fun getCount(): Int
}
