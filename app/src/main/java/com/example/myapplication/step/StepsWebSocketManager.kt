package com.example.myapplication.step

import android.util.Log
import com.example.myapplication.utils.BaseWebSocketManager

object StepsWebSocketManager : BaseWebSocketManager<Int>("StepsWS") {
    override suspend fun processFrame(message: String) {
        Log.d("StepsWS", "Received message: $message")
        val steps = message.toIntOrNull() ?: 0
        _dataFlow.emit(steps)
    }
}

object CaloriesWebSocketManager : BaseWebSocketManager<Int>("CaloriesWS") {
    override suspend fun processFrame(message: String) {
        Log.d("CaloriesWS", "Received message: $message")
        val calories = message.toIntOrNull() ?: 0
        _dataFlow.emit(calories)
    }
}
