package com.example.myapplication.step

class StepsRepository {

    val stepsFlow = StepsWebSocketManager.stepsFlow
    val caloriesFlow = CaloriesWebSocketManager.caloriesFlow

    fun connectSteps(url: String) {
        StepsWebSocketManager.connect(url)
    }

    fun connectCalories(url: String) {
        CaloriesWebSocketManager.connect(url)
    }

    fun closeConnections() {
        StepsWebSocketManager.close()
        CaloriesWebSocketManager.close()
    }
}
