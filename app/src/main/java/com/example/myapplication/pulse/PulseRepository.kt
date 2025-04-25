package com.example.myapplication.pulse

/**
 * Репозиторий для управления данными пульса.
 *
 * Предоставляет доступ к потокам данных о текущем, максимальном и минимальном пульсе,
 * а также методы для подключения и отключения WebSocket-соединений.
 */
class PulseRepository {
    val pulseFlow = PulseWebSocketManager.pulseFlow
    val maxPulseFlow = PulseWebSocketManager.maxPulseFlow
    val minPulseFlow = PulseWebSocketManager.minPulseFlow

    /**
     * Подключает WebSocket для получения данных о текущем пульсе.
     *
     * @param url URL WebSocket-сервера.
     */
    fun connectPulse(url: String) {
        PulseWebSocketManager.connectPulse(url)
    }

    /**
     * Подключает WebSocket для получения данных о максимальном пульсе.
     *
     * @param url URL WebSocket-сервера.
     */
    fun connectMaxPulse(url: String) {
        PulseWebSocketManager.connectMaxPulse(url)
    }

    /**
     * Подключает WebSocket для получения данных о минимальном пульсе.
     *
     * @param url URL WebSocket-сервера.
     */
    fun connectMinPulse(url: String) {
        PulseWebSocketManager.connectMinPulse(url)
    }

    /**
     * Закрывает все WebSocket-соединения.
     */
    fun closeConnection() {
        PulseWebSocketManager.close()
    }
}