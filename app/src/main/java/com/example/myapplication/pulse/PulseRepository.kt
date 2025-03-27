 package com.example.myapplication.pulse


class PulseRepository {
    val pulseFlow = PulseWebSocketManager.pulseFlow
    val maxPulseFlow = PulseWebSocketManager.maxPulseFlow
    val minPulseFlow = PulseWebSocketManager.minPulseFlow

    fun connectPulse(url: String) {
        PulseWebSocketManager.connectPulse(url)
    }

    fun connectMaxPulse(url: String) {
        PulseWebSocketManager.connectMaxPulse(url)
    }

    fun connectMinPulse(url: String) {
        PulseWebSocketManager.connectMinPulse(url)
    }

    fun closeConnection() {
        PulseWebSocketManager.close()
    }
}