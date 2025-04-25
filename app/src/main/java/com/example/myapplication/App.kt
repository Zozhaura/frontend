package com.example.myapplication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import com.example.myapplication.step.StepTrackingService

/**
 * Главная активность приложения, запускающая сервис отслеживания шагов и UI.
 *
 * Этот класс отвечает за инициализацию приложения, запуск [StepTrackingService] для
 * отслеживания шагов и отображение главного экрана через [AppScreen].
 */
class App : ComponentActivity() {

    /**
     * Вызывается при создании активности.
     *
     * Запускает [StepTrackingService] для отслеживания шагов (в режиме foreground на Android 8.0+
     * или обычном режиме на более ранних версиях) и устанавливает содержимое активности через
     * [AppScreen], обёрнутое в [MaterialTheme].
     *
     * @param savedInstanceState Сохранённое состояние активности (если есть).
     * @requires Android API 26 (Oreo) или выше из-за зависимости от `LocalDate` в [AppScreen].
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, StepTrackingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        setContent {
            MaterialTheme {
                AppScreen()
            }
        }
    }
}