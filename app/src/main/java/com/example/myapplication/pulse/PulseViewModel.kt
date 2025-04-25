package com.example.myapplication.pulse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для управления данными о пульсе.
 *
 * Обрабатывает подключение к WebSocket-серверу, получение данных о пульсе и обработку ошибок.
 *
 * @param repository Репозиторий для доступа к данным о пульсе.
 * @param baseUrl Базовый URL для WebSocket-соединений.
 */
class PulseViewModel(
    private val repository: PulseRepository = PulseRepository(),
    private val baseUrl: String = "ws://10.0.2.2:8081"
) : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    val pulseState = repository.pulseFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 60
    )

    val maxPulse = repository.maxPulseFlow
        .map { pulse ->
            if (pulse <= 0 || pulse > 300) 70 else pulse
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 70
        )

    val minPulse = repository.minPulseFlow
        .map { pulse ->
            if (pulse <= 0 || pulse > 300) 70 else pulse
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 70
        )

    init {
        connectToWebSockets()
    }

    /**
     * Подключается к WebSocket-серверу для получения данных о пульсе.
     */
    private fun connectToWebSockets() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                repository.connectPulse("$baseUrl/pulse")
                repository.connectMaxPulse("$baseUrl/maxpulse")
                repository.connectMinPulse("$baseUrl/minpulse")
                delay(1000)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Не удалось подключиться: ${e.message}"
                _isLoading.value = false
                retryConnection()
            }
        }
    }

    /**
     * Повторяет попытку подключения к WebSocket-серверу в случае ошибки.
     */
    private fun retryConnection() {
        viewModelScope.launch {
            delay(5000)
            if (_error.value != null && !_isLoading.value) {
                connectToWebSockets()
            }
        }
    }

    override fun onCleared() {
        repository.closeConnection()
        super.onCleared()
    }
}