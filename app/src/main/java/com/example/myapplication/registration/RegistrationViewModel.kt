package com.example.myapplication.registration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

class RegistrationViewModel : ViewModel() {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { prettyPrint = true })
        }
    }

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
                val response: String = client.post("http://10.0.2.2:8080/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.bodyAsText()
                Log.d("RegistrationVM", "Ответ от сервера: $response")
                onSuccess(response)
            } catch (e: Exception) {
                Log.e("RegistrationVM", "Ошибка регистрации: ${e.message}", e)
                onError(e.message ?: "Unknown error")
            }
        }
    }
}
