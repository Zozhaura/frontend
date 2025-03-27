 package com.example.myapplication.pulse

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class PulseTrackingService : Service() {
    override fun onCreate() {
        super.onCreate()
        Log.d("PulseTrackingService", "Service onCreate: connecting to WebSockets")
        PulseWebSocketManager.connectPulse("ws://10.0.2.2:8081/pulse")
        PulseWebSocketManager.connectMaxPulse("ws://10.0.2.2:8081/maxpulse")
        PulseWebSocketManager.connectMinPulse("ws://10.0.2.2:8081/minpulse")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("PulseTrackingService", "Service onDestroy: closing WebSockets")
        PulseWebSocketManager.close()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}