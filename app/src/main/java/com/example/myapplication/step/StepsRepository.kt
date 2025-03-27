package com.example.myapplication.step



class StepsRepository {
    val stepsFlow = StepsWebSocketManager.dataFlow
    val caloriesFlow = CaloriesWebSocketManager.dataFlow

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
