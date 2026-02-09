package com.raincast.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.raincast.data.local.entities.RadarFrameEntity
import com.raincast.data.local.entities.SavedLocationEntity
import com.raincast.data.local.entities.WeatherCacheEntity

@Database(
    entities = [
        RadarFrameEntity::class,
        SavedLocationEntity::class,
        WeatherCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun radarFrameDao(): RadarFrameDao
    abstract fun locationDao(): LocationDao
    abstract fun weatherCacheDao(): WeatherCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "raincast_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
