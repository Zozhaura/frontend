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

/**
 * Модель данных для ответа сервера о пользователе.
 *
 * @param username Имя пользователя (email).
 * @param name Имя пользователя.
 * @param height Рост.
 * @param weight Вес.
 * @param gender Пол.
 * @param goalWeight Целевой вес.
 */
@Serializable
data class UserResponse(
    val username: String,
    val name: String,
    val height: Double,
    val weight: Double,
    val gender: String,
    val goalWeight: Double,
    val age: Int = 30
)

/**
 * Модель запроса для обновления имени.
 *
 * @param name Новое имя.
 */
@Serializable
data class UpdateNameRequest(val name: String)

/**
 * Модель запроса для обновления роста.
 *
 * @param height Новый рост.
 */
@Serializable
data class UpdateHeightRequest(val height: Double)

/**
 * Модель запроса для обновления веса.
 *
 * @param weight Новый вес.
 */
@Serializable
data class UpdateWeightRequest(val weight: Double)

/**
 * Модель запроса для обновления целевого веса.
 *
 * @param goal Новый целевой вес.
 */
@Serializable
data class UpdateGoalWeightRequest(val goal: Double)

/**
 * Модель запроса для обновления имени пользователя.
 *
 * @param username Новое имя пользователя (email).
 */
@Serializable
data class UpdateUsernameRequest(val username: String)

/**
 * ViewModel для управления данными профиля пользователя.
 *
 * Отвечает за загрузку и обновление информации о пользователе, включая локальное хранение возраста.
 */
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

    /**
     * Сохраняет данные пользователя в зашифрованное хранилище.
     *
     * @param context Контекст приложения.
     * @param user Данные пользователя.
     */
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

    /**
     * Загружает данные пользователя из зашифрованного хранилища.
     *
     * @param context Контекст приложения.
     * @return Данные пользователя или null, если данных нет.
     */
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

    /**
     * Загружает информацию о пользователе.
     *
     * Сначала пытается загрузить данные из локального хранилища, затем с сервера.
     *
     * @param context Контекст приложения.
     * @param forceRefresh Принудительное обновление данных с сервера.
     */
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

                        val serverResponse = Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                        }.decodeFromString<UserResponse>(responseText)

                        val currentUser = _userResponse.value
                        val response = serverResponse.copy(
                            age = currentUser?.age ?: 30
                        )

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

    /**
     * Обновляет информацию о пользователе.
     *
     * Отправляет запросы на сервер для обновления имени, роста, веса и целевого веса, а также обновляет локальные данные.
     *
     * @param context Контекст приложения.
     * @param name Новое имя.
     * @param height Новый рост.
     * @param weight Новый вес.
     * @param goalWeight Новый целевой вес.
     * @param age Новый возраст.
     * @param username Новое имя пользователя (email).
     * @param avatarUri URI аватара (не используется в текущей реализации).
     * @param onSuccess Callback, вызываемый при успешном обновлении.
     * @param onError Callback, вызываемый при ошибке с сообщением.
     */
    fun updateUserInfo(
        context: Context,
        name: String,
        height: Double,
        weight: Double,
        goalWeight: Double,
        username: String,
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
                val nameResponse = client.post("http://10.0.2.2:8080/auth/updatename") {
                    header("Authorization", "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(UpdateNameRequest(name))
                }
                if (nameResponse.status != HttpStatusCode.OK) {
                    onError("Ошибка обновления имени: ${nameResponse.status}, ${nameResponse.bodyAsText()}")
                    _isUpdating.value = false
                    return@launch
                }

                val heightResponse = client.post("http://10.0.2.2:8080/auth/updateheight") {
                    header("Authorization", "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(UpdateHeightRequest(height))
                }
                if (heightResponse.status != HttpStatusCode.OK) {
                    onError("Ошибка обновления роста: ${heightResponse.status}, ${heightResponse.bodyAsText()}")
                    _isUpdating.value = false
                    return@launch
                }

                val weightResponse = client.post("http://10.0.2.2:8080/auth/updateweight") {
                    header("Authorization", "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(UpdateWeightRequest(weight))
                }
                if (weightResponse.status != HttpStatusCode.OK) {
                    onError("Ошибка обновления веса: ${weightResponse.status}, ${weightResponse.bodyAsText()}")
                    _isUpdating.value = false
                    return@launch
                }

                val goalWeightResponse = client.post("http://10.0.2.2:8080/auth/updategoal") {
                    header("Authorization", "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(UpdateGoalWeightRequest(goalWeight))
                }
                if (goalWeightResponse.status != HttpStatusCode.OK) {
                    onError("Ошибка обновления целевого веса: ${goalWeightResponse.status}, ${goalWeightResponse.bodyAsText()}")
                    _isUpdating.value = false
                    return@launch
                }

                _userResponse.value = _userResponse.value?.copy(
                    name = name,
                    height = height,
                    weight = weight,
                    goalWeight = goalWeight,
                    username = username
                ) ?: UserResponse(
                    username = username,
                    name = name,
                    height = height,
                    weight = weight,
                    gender = _userResponse.value?.gender ?: "unknown",
                    goalWeight = goalWeight,
                )
                _userResponse.value?.let { saveUserToPrefs(context, it) }
                onSuccess()
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