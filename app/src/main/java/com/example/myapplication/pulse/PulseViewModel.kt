package com.example.myapplication.pulse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    val maxPulse = repository.maxPulseFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val minPulse = repository.minPulseFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        connectToWebSockets()
    }

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