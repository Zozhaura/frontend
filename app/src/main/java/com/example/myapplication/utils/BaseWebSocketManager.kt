package com.example.myapplication.utils

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Абстрактный класс для управления WebSocket-соединением.
 *
 * Этот класс предоставляет базовую функциональность для подключения к WebSocket-серверу,
 * обработки входящих сообщений и управления жизненным циклом соединения. Данные передаются
 * через [dataFlow] в виде объектов типа [T].
 *
 * @param T Тип данных, который будет передаваться через WebSocket.
 * @param clientName Имя клиента для логирования (по умолчанию "BaseWS").
 */
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

    /**
     * Подключается к WebSocket-серверу по указанному URL.
     *
     * Если соединение уже активно, метод ничего не делает. В случае успешного подключения
     * начинает принимать входящие сообщения и передавать их в [processFrame].
     *
     * @param url URL WebSocket-сервера.
     */
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

    /**
     * Обрабатывает входящее текстовое сообщение из WebSocket.
     *
     * Этот метод должен быть реализован в подклассах для обработки сообщений
     * и преобразования их в объекты типа [T] для передачи через [dataFlow].
     *
     * @param message Текстовое сообщение, полученное через WebSocket.
     */
    protected abstract suspend fun processFrame(message: String)

    /**
     * Закрывает WebSocket-соединение.
     *
     * Отменяет текущую корутину, закрывает сессию и сбрасывает состояние.
     */
    fun close() {
        Log.d(clientName, "Closing connection")
        job?.cancel()
        job = null
        session?.cancel()
        session = null
    }
}