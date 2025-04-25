package com.example.myapplication.step

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel для управления данными о шагах и калориях.
 *
 * Комбинирует шаги из WebSocket и вручную добавленные шаги, а также вычисляет потраченные калории.
 *
 * @param repository Репозиторий для получения данных о шагах.
 */
class StepViewModel(
    private val repository: StepsRepository = StepsRepository()
) : ViewModel() {

    private val manualSteps = MutableStateFlow(0)

    val stepsState: StateFlow<Int> = repository.stepsFlow
        .combine(manualSteps) { webSocketSteps, manual ->
            webSocketSteps + manual
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val caloriesState: StateFlow<Int> = stepsState
        .map { totalSteps ->
            (totalSteps * 0.04).toInt()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    /**
     * Добавляет ручные шаги к общему количеству.
     *
     * @param stepsToAdd Количество шагов для добавления.
     */
    fun addSteps(stepsToAdd: Int) {
        manualSteps.value += stepsToAdd
    }

    /**
     * Сбрасывает шаги до 0.
     */
    fun resetSteps() {
        manualSteps.value = 0
        repository.resetSteps()
    }
}