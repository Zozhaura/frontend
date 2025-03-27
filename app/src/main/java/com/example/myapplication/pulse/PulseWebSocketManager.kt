 package com.example.myapplication.pulse

import android.util.Log
import com.example.myapplication.utils.BaseWebSocketManager
import kotlinx.coroutines.flow.MutableSharedFlow

object PulseWebSocketManager {
    private val _pulseFlow = MutableSharedFlow<Int>(replay = 1)
    val pulseFlow = _pulseFlow

    private val _maxPulseFlow = MutableSharedFlow<Int>(replay = 1)
    val maxPulseFlow = _maxPulseFlow

    private val _minPulseFlow = MutableSharedFlow<Int>(replay = 1)
    val minPulseFlow = _minPulseFlow

    private val pulseManager = object : BaseWebSocketManager<Int>("PulseWS") {
        override suspend fun processFrame(message: String) {
            Log.d("PulseWS", "Received pulse: $message")
            val pulse = message.toIntOrNull() ?: 0
            _pulseFlow.emit(pulse)
        }
    }

    private val maxPulseManager = object : BaseWebSocketManager<Int>("MaxPulseWS") {
        override suspend fun processFrame(message: String) {
            Log.d("MaxPulseWS", "Received max pulse: $message")
            val maxPulse = message.toIntOrNull() ?: 0
            _maxPulseFlow.emit(maxPulse)
        }
    }

    private val minPulseManager = object : BaseWebSocketManager<Int>("MinPulseWS") {
        override suspend fun processFrame(message: String) {
            Log.d("MinPulseWS", "Received min pulse: $message")
            val minPulse = message.toIntOrNull() ?: 0
            _minPulseFlow.emit(minPulse)
        }
    }

    fun connectPulse(url: String) {
        pulseManager.connect(url)
    }

    fun connectMaxPulse(url: String) {
        maxPulseManager.connect(url)
    }

    fun connectMinPulse(url: String) {
        minPulseManager.connect(url)
    }

    fun close() {
        pulseManager.close()
        maxPulseManager.close()
        minPulseManager.close()
    }
}