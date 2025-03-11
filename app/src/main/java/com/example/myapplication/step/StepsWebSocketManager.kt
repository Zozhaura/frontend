package com.example.myapplication.step

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object StepsWebSocketManager {

    private val client = HttpClient(OkHttp) {
        install(WebSockets)
    }
    private var session: DefaultWebSocketSession? = null
    private val _stepsFlow = MutableSharedFlow<Int>(replay = 1)
    val stepsFlow = _stepsFlow.asSharedFlow()

    private var job: Job? = null
    fun connect(url: String) {
        if (job?.isActive == true) return

        Log.d("StepsWebSocketManager", "Attempting to connect to $url")

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                client.webSocket(urlString = url) {
                    session = this
                    Log.d("StepsWebSocketManager", "Connected to $url")

                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            Log.d("StepsWebSocketManager", "Received message: $text")
                            val steps = text.toIntOrNull() ?: 0
                            _stepsFlow.emit(steps)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("StepsWebSocketManager", "Error connecting to $url: ${e.message}", e)
            }
        }
    }

    fun close() {
        Log.d("StepsWebSocketManager", "Closing connection")
        job?.cancel()
        job = null
        session?.cancel()
        session = null
    }
}
