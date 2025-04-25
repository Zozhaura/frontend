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

/**
 * Сервис для фонового отслеживания шагов и калорий через WebSocket.
 *
 * Запускает WebSocket-соединения для получения данных о шагах и калориях и работает в режиме foreground.
 */
class StepTrackingService : Service() {

    private val CHANNEL_ID = "StepTrackingServiceChannel"

    /**
     * Вызывается при создании сервиса.
     *
     * Создаёт канал уведомлений, запускает сервис в режиме foreground и подключается к WebSocket.
     */
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, getNotification())
        Log.d("StepTrackingService", "Service onCreate: connecting to WebSockets")
        StepsWebSocketManager.connect("ws://10.0.2.2:8081/steps")
        CaloriesWebSocketManager.connect("ws://10.0.2.2:8081/calories")
    }

    /**
     * Вызывается при уничтожении сервиса.
     *
     * Закрывает WebSocket-соединения.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d("StepTrackingService", "Service onDestroy: closing WebSockets")
        StepsWebSocketManager.close()
        CaloriesWebSocketManager.close()
    }

    /**
     * Возвращает IBinder для привязки сервиса.
     *
     * @param intent Intent, переданный при привязке.
     * @return `null`, так как сервис не поддерживает привязку.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Создаёт уведомление для работы сервиса в режиме foreground.
     *
     * @return Уведомление с заголовком и текстом о работе сервиса.
     */
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

    /**
     * Создаёт канал уведомлений для Android 8.0 и выше.
     */
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