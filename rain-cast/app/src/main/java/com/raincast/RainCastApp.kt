package com.raincast

import android.app.Application
import com.raincast.di.AppModule
import com.raincast.notifications.NotificationChannels
import org.osmdroid.config.Configuration

class RainCastApp : Application() {

    lateinit var appModule: AppModule
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        appModule = AppModule(this)

        // Configure osmdroid
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = cacheDir
            osmdroidTileCache = cacheDir.resolve("tiles")
        }

        // Create notification channels
        NotificationChannels.createChannels(this)
    }

    companion object {
        lateinit var instance: RainCastApp
            private set
    }
}
