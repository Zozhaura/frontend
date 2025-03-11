package com.example.myapplication.step

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.myapplication.R

class StepTrackingService : Service() {

    private val CHANNEL_ID = "StepTrackingServiceChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, getNotification())
        Log.d("StepTrackingService", "Service onCreate: connecting to WebSockets")
        StepsWebSocketManager.connect("ws://10.0.2.2:8081/steps")
        CaloriesWebSocketManager.connect("ws://10.0.2.2:8081/calories")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("StepTrackingService", "Service onDestroy: closing WebSockets")
        StepsWebSocketManager.close()
        CaloriesWebSocketManager.close()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun getNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Tracking Service")
                .setContentText("Tracking steps and calories in background")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Step Tracking Service")
                .setContentText("Tracking steps and calories in background")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracking Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
