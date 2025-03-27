 package com.example.myapplication.pulse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class PulseViewModel(
    private val repository: PulseRepository = PulseRepository()
) : ViewModel() {
    val pulseState = repository.pulseFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
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
        repository.connectPulse("ws://10.0.2.2:8081/pulse")
        repository.connectMaxPulse("ws://10.0.2.2:8081/maxpulse")
        repository.connectMinPulse("ws://10.0.2.2:8081/minpulse")
    }

    override fun onCleared() {
        super.onCleared()
        repository.closeConnection()
    }
}