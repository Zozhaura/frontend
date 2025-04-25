package com.example.myapplication.step

/**
 * Репозиторий для управления данными о шагах и калориях через WebSocket.
 *
 * Предоставляет поток шагов и методы для подключения и закрытия соединений.
 */
class StepsRepository {
    val stepsFlow = StepsWebSocketManager.dataFlow

    /**
     * Подключается к WebSocket для получения данных о шагах.
     *
     * @param url URL WebSocket-сервера для шагов.
     */
    fun connectSteps(url: String) {
        StepsWebSocketManager.connect(url)
    }

    /**
     * Подключается к WebSocket для получения данных о калориях.
     *
     * @param url URL WebSocket-сервера для калорий.
     */
    fun connectCalories(url: String) {
        CaloriesWebSocketManager.connect(url)
    }

    /**
     * Закрывает все WebSocket-соединения.
     */
    fun closeConnections() {
        StepsWebSocketManager.close()
        CaloriesWebSocketManager.close()
    }

    /**
     * Сбрасывает шаги до 0 через WebSocket.
     */
    fun resetSteps() {
        StepsWebSocketManager.reset()
    }
}