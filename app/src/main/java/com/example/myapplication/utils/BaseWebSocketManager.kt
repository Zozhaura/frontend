package com.example.myapplication.utils

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class BaseWebSocketManager<T>(
    private val clientName: String = "BaseWS"
) {
    protected val client = HttpClient(OkHttp) {
        install(WebSockets)
    }

    private var session: DefaultWebSocketSession? = null
    private var job: Job? = null

    protected val _dataFlow = MutableSharedFlow<T>(replay = 1)
    val dataFlow = _dataFlow.asSharedFlow()

    fun connect(url: String) {
        if (job?.isActive == true) return
        Log.d(clientName, "Attempting to connect to $url")
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                client.webSocket(urlString = url) {
                    session = this
                    Log.d(clientName, "Connected to $url")
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            processFrame(frame.readText())
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(clientName, "Error connecting to $url: ${e.message}", e)
            }
        }
    }
    protected abstract suspend fun processFrame(message: String)

    fun close() {
        Log.d(clientName, "Closing connection")
        job?.cancel()
        job = null
        session?.cancel()
        session = null
    }
}
