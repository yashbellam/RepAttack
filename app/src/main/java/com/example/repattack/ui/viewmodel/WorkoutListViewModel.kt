package com.example.repattack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repattack.data.model.Workout
import com.example.repattack.data.repository.RepAttackRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutListViewModel(
    private val repository: RepAttackRepository
) : ViewModel() {

    /**
     * All workouts, exposed as StateFlow so Compose recomposes when data changes.
     * stateIn converts the Room Flow into a StateFlow that:
     * - Starts collecting when the first subscriber appears (WhileSubscribed)
     * - Stops 5 seconds after the last subscriber disappears (saves resources during config changes)
     * - Starts with an empty list
     */
    val workouts: StateFlow<List<Workout>> = repository.getAllWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addWorkout(name: String, description: String = "") {
        viewModelScope.launch {
            repository.insertWorkout(Workout(name = name, description = description))
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
        }
    }

    fun updateWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.updateWorkout(workout)
        }
    }
}
