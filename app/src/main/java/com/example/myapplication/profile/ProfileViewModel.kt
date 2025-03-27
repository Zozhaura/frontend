 package com.example.myapplication.profile

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.utils.TokenManager
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class UserResponse(
    val username: String,
    val name: String,
    val height: Double,
    val weight: Double,
    val gender: String,
    val goalWeight: Double
)

class ProfileViewModel : ViewModel() {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
    }

    private val _userResponse: MutableState<UserResponse?> = mutableStateOf(null)
    val userResponse: UserResponse? get() = _userResponse.value

    private val _errorMessage: MutableState<String?> = mutableStateOf(null)
    val errorMessage: String? get() = _errorMessage.value

    fun fetchUserInfo(context: Context) {
        val token = TokenManager.getToken(context)
        if (token == null) {
            _errorMessage.value = "Токен отсутствует. Пожалуйста, войдите снова."
            return
        }
        Log.d("ProfileViewModel", "Извлеченный токен: $token")

        viewModelScope.launch {
            try {
                val httpResponse = client.get("http://10.0.2.2:8080/auth/userinfo") {
                    header("Authorization", "Bearer $token")
                    header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                }

                when (httpResponse.status) {
                    HttpStatusCode.OK -> {
                        val responseText = httpResponse.bodyAsText()
                        if (responseText.isBlank() || !responseText.trim().startsWith("{")) {
                            throw IllegalStateException("Получен некорректный JSON: $responseText")
                        }

                        val response = Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                        }.decodeFromString<UserResponse>(responseText)

                        _userResponse.value = response
                        _errorMessage.value = null
                    }
                    HttpStatusCode.Unauthorized -> {
                        val errorText = httpResponse.bodyAsText()
                        _errorMessage.value = "Ошибка авторизации: $errorText"
                        TokenManager.clearToken(context)
                    }
                    HttpStatusCode.NotFound -> {
                        val errorText = httpResponse.bodyAsText()
                        _errorMessage.value = "Пользователь не найден: $errorText"
                    }
                    else -> {
                        val errorText = httpResponse.bodyAsText()
                        _errorMessage.value = "Ошибка сервера: ${httpResponse.status} - $errorText"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки данных: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}