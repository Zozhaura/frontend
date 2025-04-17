package com.example.myapplication.login

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.ConnectException

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(val token: String)

class LoginViewModel : ViewModel() {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loginUser(
        username: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = client.post("http://10.0.2.2:8080/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(username, password))
                }.bodyAsText()

                val authResponse = Json.decodeFromString<AuthResponse>(response)
                onSuccess(authResponse.token)
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is ConnectException -> "Не удалось подключиться к серверу"
                    else -> e.message ?: "Неизвестная ошибка"
                }
                onError(errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}