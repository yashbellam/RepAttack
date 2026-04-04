package com.example.repattack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repattack.data.model.Exercise
import com.example.repattack.data.model.Workout
import com.example.repattack.data.repository.RepAttackRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutDetailViewModel(
    private val repository: RepAttackRepository
) : ViewModel() {

    private val _workoutId = MutableStateFlow<Long?>(null)
    private val _workout = MutableStateFlow<Workout?>(null)

    /** The workout being viewed/edited. */
    val workout: StateFlow<Workout?> = _workout.asStateFlow()

    /** Exercises belonging to this workout, in order. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val exercises: StateFlow<List<Exercise>> = _workoutId
        .flatMapLatest { id ->
            if (id != null) repository.getExercisesForWorkout(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun loadWorkout(workoutId: Long) {
        _workoutId.value = workoutId
        viewModelScope.launch {
            _workout.value = repository.getWorkoutById(workoutId)
        }
    }

    fun addExercise(
        workoutId: Long,
        name: String,
        targetSets: Int?,
        minReps: Int?,
        maxReps: Int?,
        restTime: String,
        notes: String,
        url: String
    ) {
        viewModelScope.launch {
            val currentExercises = exercises.value
            repository.insertExercise(
                Exercise(
                    workoutId = workoutId,
                    name = name,
                    targetSets = targetSets,
                    minReps = minReps,
                    maxReps = maxReps,
                    restTime = restTime,
                    notes = notes,
                    url = url,
                    orderIndex = currentExercises.size
                )
            )
        }
    }

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.updateExercise(exercise)
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }

    fun duplicateExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.insertExercise(
                exercise.copy(
                    id = 0,
                    name = "${exercise.name} (copy)",
                    orderIndex = exercises.value.size
                )
            )
        }
    }

    fun updateWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.updateWorkout(workout)
            _workout.value = workout
        }
    }
}
