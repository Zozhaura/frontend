package com.example.myapplication.profile

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.utils.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
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

    private val _isLoading: MutableState<Boolean> = mutableStateOf(true)
    val isLoading: Boolean get() = _isLoading.value
    private fun saveUserToPrefs(context: Context, user: UserResponse) {
        val prefs = context.getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString("user_json", Json.encodeToString(UserResponse.serializer(), user))
            apply()
        }
    }

    private fun loadUserFromPrefs(context: Context): UserResponse? {
        val prefs = context.getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
        val json = prefs.getString("user_json", null) ?: return null
        return try {
            Json.decodeFromString<UserResponse>(json)
        } catch (e: Exception) {
            null
        }
    }

    fun fetchUserInfo(context: Context, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            if (!forceRefresh && _userResponse.value == null) {
                loadUserFromPrefs(context)?.let {
                    _userResponse.value = it
                }
            }

            val token = TokenManager.getToken(context)
            if (token == null) {
                _errorMessage.value = "Токен отсутствует. Пожалуйста, войдите снова."
                _isLoading.value = false
                return@launch
            }

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
                        saveUserToPrefs(context, response)
                        _errorMessage.value = null
                    }
                    HttpStatusCode.Unauthorized -> {
                        _errorMessage.value = "Ошибка авторизации"
                        TokenManager.clearToken(context)
                    }
                    else -> {
                        _errorMessage.value = "Ошибка сервера: ${httpResponse.status}"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки данных: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        client.close()
        super.onCleared()
    }
}