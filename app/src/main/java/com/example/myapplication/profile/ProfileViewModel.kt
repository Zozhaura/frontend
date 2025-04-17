package com.example.myapplication.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.myapplication.utils.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
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
    val goalWeight: Double,
    val proteinCurrent: Double = 0.0,
    val proteinTarget: Double = 0.0,
    val fatCurrent: Double = 0.0,
    val fatTarget: Double = 0.0,
    val carbsCurrent: Double = 0.0,
    val carbsTarget: Double = 0.0,
    val caloriesCurrent: Double = 0.0,
    val caloriesTarget: Double = 0.0
)

@Serializable
data class UpdateUserRequest(
    val name: String,
    val height: Double,
    val weight: Double,
    val proteinCurrent: Double,
    val proteinTarget: Double,
    val fatCurrent: Double,
    val fatTarget: Double,
    val carbsCurrent: Double,
    val carbsTarget: Double,
    val caloriesCurrent: Double,
    val caloriesTarget: Double
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
    val userResponse: State<UserResponse?> get() = _userResponse

    private val _errorMessage: MutableState<String?> = mutableStateOf(null)
    val errorMessage: State<String?> get() = _errorMessage

    private val _isLoading: MutableState<Boolean> = mutableStateOf(true)
    val isLoading: State<Boolean> get() = _isLoading

    private val _isUpdating: MutableState<Boolean> = mutableStateOf(false)
    val isUpdating: State<Boolean> get() = _isUpdating

    private fun saveUserToPrefs(context: Context, user: UserResponse) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val prefs = EncryptedSharedPreferences.create(
            "ProfilePrefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        with(prefs.edit()) {
            putString("user_json", Json.encodeToString(UserResponse.serializer(), user))
            apply()
        }
    }

    private fun loadUserFromPrefs(context: Context): UserResponse? {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val prefs = EncryptedSharedPreferences.create(
            "ProfilePrefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
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

    fun updateUserInfo(
        context: Context,
        name: String,
        height: Double,
        weight: Double,
        avatarUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isUpdating.value = true
            _errorMessage.value = null

            val token = TokenManager.getToken(context)
            if (token == null) {
                _errorMessage.value = "Токен отсутствует. Пожалуйста, войдите снова."
                _isUpdating.value = false
                return@launch
            }

            try {
                val currentUser = _userResponse.value
                val request = UpdateUserRequest(
                    name = name,
                    height = height,
                    weight = weight,
                    proteinCurrent = currentUser?.proteinCurrent ?: 0.0,
                    proteinTarget = currentUser?.proteinTarget ?: 0.0,
                    fatCurrent = currentUser?.fatCurrent ?: 0.0,
                    fatTarget = currentUser?.fatTarget ?: 0.0,
                    carbsCurrent = currentUser?.carbsCurrent ?: 0.0,
                    carbsTarget = currentUser?.carbsTarget ?: 0.0,
                    caloriesCurrent = currentUser?.caloriesCurrent ?: 0.0,
                    caloriesTarget = currentUser?.caloriesTarget ?: 0.0
                )

                val httpResponse = client.post("http://10.0.2.2:8080/auth/update") {
                    header("Authorization", "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                when (httpResponse.status) {
                    HttpStatusCode.OK -> {
                        // Обновляем локальные данные
                        _userResponse.value = _userResponse.value?.copy(
                            name = name,
                            height = height,
                            weight = weight
                        )
                        _userResponse.value?.let { saveUserToPrefs(context, it) }
                        onSuccess()
                    }
                    HttpStatusCode.Unauthorized -> {
                        _errorMessage.value = "Ошибка авторизации"
                        TokenManager.clearToken(context)
                        onError("Ошибка авторизации")
                    }
                    else -> {
                        onError("Ошибка сервера: ${httpResponse.status}")
                    }
                }
            } catch (e: Exception) {
                onError("Ошибка обновления: ${e.message}")
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun updateMacrosAndCalories(
        context: Context,
        proteinCurrent: Double,
        proteinTarget: Double,
        fatCurrent: Double,
        fatTarget: Double,
        carbsCurrent: Double,
        carbsTarget: Double,
        caloriesCurrent: Double,
        caloriesTarget: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isUpdating.value = true
            _errorMessage.value = null

            val token = TokenManager.getToken(context)
            if (token == null) {
                _errorMessage.value = "Токен отсутствует. Пожалуйста, войдите снова."
                _isUpdating.value = false
                return@launch
            }

            try {
                val currentUser = _userResponse.value
                if (currentUser == null) {
                    onError("Пользовательские данные отсутствуют")
                    return@launch
                }
                _userResponse.value = currentUser.copy(
                    proteinCurrent = proteinCurrent,
                    proteinTarget = proteinTarget,
                    fatCurrent = fatCurrent,
                    fatTarget = fatTarget,
                    carbsCurrent = carbsCurrent,
                    carbsTarget = carbsTarget,
                    caloriesCurrent = caloriesCurrent,
                    caloriesTarget = caloriesTarget
                )

                _userResponse.value?.let { saveUserToPrefs(context, it) }
                onSuccess()

                // Раскомментировать, когда сервер будет готов
                /*
                val request = UpdateUserRequest(
                    name = currentUser.name,
                    height = currentUser.height,
                    weight = currentUser.weight,
                    proteinCurrent = proteinCurrent,
                    proteinTarget = proteinTarget,
                    fatCurrent = fatCurrent,
                    fatTarget = fatTarget,
                    carbsCurrent = carbsCurrent,
                    carbsTarget = carbsTarget,
                    caloriesCurrent = caloriesCurrent,
                    caloriesTarget = caloriesTarget
                )

                val httpResponse = client.post("http://10.0.2.2:8080/auth/update") {
                    header("Authorization", "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                when (httpResponse.status) {
                    HttpStatusCode.OK -> {
                        _userResponse.value = _userResponse.value?.copy(
                            proteinCurrent = proteinCurrent,
                            proteinTarget = proteinTarget,
                            fatCurrent = fatCurrent,
                            fatTarget = fatTarget,
                            carbsCurrent = carbsCurrent,
                            carbsTarget = carbsTarget,
                            caloriesCurrent = caloriesCurrent,
                            caloriesTarget = caloriesTarget
                        )
                        _userResponse.value?.let { saveUserToPrefs(context, it) }
                        onSuccess()
                    }
                    HttpStatusCode.Unauthorized -> {
                        _errorMessage.value = "Ошибка авторизации"
                        TokenManager.clearToken(context)
                        onError("Ошибка авторизации")
                    }
                    else -> {
                        onError("Ошибка сервера: ${httpResponse.status}")
                    }
                }
                */
            } catch (e: Exception) {
                onError("Ошибка обновления: ${e.message}")
            } finally {
                _isUpdating.value = false
            }
        }
    }

    override fun onCleared() {
        client.close()
        super.onCleared()
    }
}