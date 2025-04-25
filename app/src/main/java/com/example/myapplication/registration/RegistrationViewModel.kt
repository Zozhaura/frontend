package com.example.myapplication.registration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.ConnectException

/**
 * Модель данных запроса на регистрацию.
 *
 * @param username Имя пользователя (email).
 * @param password Пароль.
 * @param name Имя пользователя.
 * @param height Рост.
 * @param weight Вес.
 * @param gender Пол.
 * @param goalWeight Целевой вес.
 */
@Serializable
data class AuthRequest(
    val username: String,
    val password: String,
    val name: String,
    val height: Double,
    val weight: Double,
    val gender: String,
    val goalWeight: Double
)

/**
 * Модель данных ответа на запрос регистрации.
 *
 * @param token Токен авторизации.
 */
@Serializable
data class AuthResponse(val token: String)

/**
 * ViewModel для управления процессом регистрации.
 *
 * Отвечает за отправку запроса на регистрацию и обработку ответа от сервера.
 */
class RegistrationViewModel : ViewModel() {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
    }

    /**
     * Выполняет регистрацию пользователя.
     *
     * Отправляет запрос на сервер с данными пользователя.
     *
     * @param username Имя пользователя (email).
     * @param password Пароль.
     * @param name Имя пользователя.
     * @param height Рост.
     * @param weight Вес.
     * @param gender Пол.
     * @param goalWeight Целевой вес.
     * @param onSuccess Callback, вызываемый при успешной регистрации с токеном.
     * @param onError Callback, вызываемый при ошибке с сообщением.
     */
    fun registerUser(
        username: String,
        password: String,
        name: String,
        height: Double,
        weight: Double,
        gender: String,
        goalWeight: Double,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = AuthRequest(
            username = username,
            password = password,
            name = name,
            height = height,
            weight = weight,
            gender = gender,
            goalWeight = goalWeight
        )
        viewModelScope.launch {
            try {
                val response = client.post("http://10.0.2.2:8080/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.bodyAsText()
                val authResponse = Json.decodeFromString<AuthResponse>(response)
                if (authResponse.token.isNotEmpty()) {
                    onSuccess(authResponse.token)
                } else {
                    onError("Token is empty")
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is ConnectException -> "Не удалось подключиться к серверу"
                    else -> e.message ?: "Неизвестная ошибка"
                }
                Log.e("RegistrationVM", "Ошибка регистрации: $errorMessage", e)
                onError(errorMessage)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}