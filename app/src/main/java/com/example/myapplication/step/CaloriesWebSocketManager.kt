package com.example.myapplication.step

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object CaloriesWebSocketManager {

    private val client = HttpClient(OkHttp) {
        install(WebSockets)
    }

    private var session: DefaultWebSocketSession? = null

    private val _caloriesFlow = MutableSharedFlow<Int>(replay = 1)
    val caloriesFlow = _caloriesFlow.asSharedFlow()

    private var job: Job? = null

    fun connect(url: String) {
        if (job?.isActive == true) return

        Log.d("CaloriesWS", "Attempting to connect to $url")
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                client.webSocket(urlString = url) {
                    session = this
                    Log.d("CaloriesWS", "Connected to $url")
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            Log.d("CaloriesWS", "Received message: $text")
                            val calories = text.toIntOrNull() ?: 0
                            _caloriesFlow.emit(calories)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CaloriesWS", "Error connecting to $url: ${e.message}", e)
            }
        }
    }

    fun close() {
        Log.d("CaloriesWS", "Closing connection")
        job?.cancel()
        job = null
        session?.cancel()
        session = null
    }
}
