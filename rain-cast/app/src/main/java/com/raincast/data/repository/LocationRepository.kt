package com.raincast.data.repository

import com.raincast.data.local.LocationDao
import com.raincast.data.local.entities.SavedLocationEntity
import kotlinx.coroutines.flow.Flow

class LocationRepository(
    private val dao: LocationDao
) {
    val savedLocations: Flow<List<SavedLocationEntity>> = dao.getAllLocations()

    suspend fun saveLocation(label: String, latitude: Double, longitude: Double): Long {
        val count = dao.getCount()
        if (count >= 5) {
            // Max 5 saved locations
            val all = dao.getAllLocationsList()
            if (all.size >= 5) {
                dao.deleteLocation(all.last())
            }
        }
        return dao.insertLocation(
            SavedLocationEntity(
                label = label,
                latitude = latitude,
                longitude = longitude
            )
        )
    }

    suspend fun deleteLocation(location: SavedLocationEntity) {
        dao.deleteLocation(location)
    }

    suspend fun setActiveLocation(locationId: Long) {
        dao.deactivateAll()
        dao.activateLocation(locationId)
    }

    suspend fun getActiveLocation(): SavedLocationEntity? {
        return dao.getActiveLocation()
    }

    suspend fun clearActiveLocation() {
        dao.deactivateAll()
    }
}
