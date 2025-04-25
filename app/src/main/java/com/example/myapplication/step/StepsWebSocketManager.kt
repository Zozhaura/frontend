package com.example.myapplication.step

import android.util.Log
import com.example.myapplication.utils.BaseWebSocketManager

/**
 * Объект для управления WebSocket-соединением для получения данных о шагах.
 *
 * Наследуется от [BaseWebSocketManager] и обрабатывает сообщения о шагах.
 */
object StepsWebSocketManager : BaseWebSocketManager<Int>("StepsWS") {
    /**
     * Обрабатывает входящее сообщение и преобразует его в количество шагов.
     *
     * @param message Текстовое сообщение, полученное через WebSocket.
     */
    override suspend fun processFrame(message: String) {
        Log.d("StepsWS", "Received message: $message")
        val steps = message.toIntOrNull() ?: 0
        _dataFlow.emit(steps)
    }

    /**
     * Сбрасывает шаги до 0.
     */
    fun reset() {
        _dataFlow.tryEmit(0)
    }
}

/**
 * Объект для управления WebSocket-соединением для получения данных о калориях.
 *
 * Наследуется от [BaseWebSocketManager] и обрабатывает сообщения о калориях.
 */
object CaloriesWebSocketManager : BaseWebSocketManager<Int>("CaloriesWS") {
    /**
     * Обрабатывает входящее сообщение и преобразует его в количество калорий.
     *
     * @param message Текстовое сообщение, полученное через WebSocket.
     */
    override suspend fun processFrame(message: String) {
        Log.d("CaloriesWS", "Received message: $message")
        val calories = message.toIntOrNull() ?: 0
        _dataFlow.emit(calories)
    }
}