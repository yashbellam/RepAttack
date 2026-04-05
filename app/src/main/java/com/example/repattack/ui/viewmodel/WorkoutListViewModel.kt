package com.example.repattack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repattack.data.model.Exercise
import com.example.repattack.data.model.Workout
import com.example.repattack.data.repository.RepAttackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutListViewModel(
    private val repository: RepAttackRepository
) : ViewModel() {

    private val dbWorkouts: StateFlow<List<Workout>> = repository.getAllWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()

    private var isDragging = false

    init {
        viewModelScope.launch {
            dbWorkouts.collect { dbList ->
                if (!isDragging) {
                    _workouts.value = dbList
                }
            }
        }
    }

    fun addWorkout(name: String, description: String = "") {
        viewModelScope.launch {
            val currentSize = _workouts.value.size
            repository.insertWorkout(Workout(name = name, description = description, orderIndex = currentSize))
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

    fun duplicateWorkout(workout: Workout) {
        viewModelScope.launch {
            // Insert right after the original by shifting subsequent workouts
            val allWorkouts = _workouts.value.toMutableList()
            val insertIndex = allWorkouts.indexOfFirst { it.id == workout.id } + 1

            val newId = repository.insertWorkout(
                Workout(
                    name = "${workout.name} (copy)",
                    description = workout.description,
                    orderIndex = insertIndex
                )
            )
            // Shift orderIndex for workouts after the insert point
            val updated = allWorkouts.filterIndexed { i, _ -> i >= insertIndex }.map {
                it.copy(orderIndex = it.orderIndex + 1)
            }
            if (updated.isNotEmpty()) repository.updateWorkouts(updated)

            val exercises = repository.getExercisesForWorkoutOnce(workout.id)
            exercises.forEach { exercise ->
                repository.insertExercise(
                    exercise.copy(id = 0, workoutId = newId)
                )
            }
        }
    }

    fun moveWorkout(fromIndex: Int, toIndex: Int) {
        val current = _workouts.value.toMutableList()
        if (fromIndex !in current.indices || toIndex !in current.indices) return
        isDragging = true
        val item = current.removeAt(fromIndex)
        current.add(toIndex, item)
        _workouts.value = current
    }

    fun commitReorder() {
        isDragging = false
        val updated = _workouts.value.mapIndexed { index, workout ->
            workout.copy(orderIndex = index)
        }
        viewModelScope.launch {
            repository.updateWorkouts(updated)
        }
    }
}
