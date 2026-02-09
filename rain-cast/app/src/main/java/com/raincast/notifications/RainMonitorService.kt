package com.raincast.notifications

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.ServiceCompat

class RainMonitorService : Service() {

    private val notificationManager by lazy { RainNotificationManager(this) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = notificationManager.getMonitorNotification()
        startForeground(RainNotificationManager.MONITOR_SERVICE_NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
