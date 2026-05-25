package com.repattack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repattack.data.model.ExerciseCatalog
import com.repattack.data.model.Workout
import com.repattack.data.model.WorkoutExercise
import com.repattack.data.model.WorkoutExerciseWithCatalog
import com.repattack.data.repository.RepAttackRepository
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
    val workout: StateFlow<Workout?> = _workout.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dbExercises: StateFlow<List<WorkoutExerciseWithCatalog>> = _workoutId
        .flatMapLatest { id ->
            if (id != null) repository.getExercisesForWorkout(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _exercises = MutableStateFlow<List<WorkoutExerciseWithCatalog>>(emptyList())
    val exercises: StateFlow<List<WorkoutExerciseWithCatalog>> = _exercises.asStateFlow()

    val allCatalogExercises: StateFlow<List<ExerciseCatalog>> = repository.getAllCatalogExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            dbExercises.collect { dbList ->
                if (_exercises.value.isEmpty() || !isDragging) {
                    _exercises.value = dbList
                }
            }
        }
    }

    private var isDragging = false

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
            // Find or create catalog entry
            var catalog = repository.getCatalogExerciseByName(name)
            if (catalog == null) {
                val catalogId = repository.insertCatalogExercise(
                    ExerciseCatalog(name = name, url = url, notes = notes)
                )
                catalog = ExerciseCatalog(id = catalogId, name = name, url = url, notes = notes)
            }

            val currentExercises = exercises.value
            repository.insertWorkoutExercise(
                WorkoutExercise(
                    workoutId = workoutId,
                    exerciseId = catalog.id,
                    targetSets = targetSets,
                    minReps = minReps,
                    maxReps = maxReps,
                    restTime = restTime,
                    orderIndex = currentExercises.size
                )
            )
        }
    }

    /** Add an existing catalog exercise to this workout with default settings. */
    fun addExerciseFromCatalog(workoutId: Long, catalog: ExerciseCatalog) {
        viewModelScope.launch {
            val currentExercises = exercises.value
            repository.insertWorkoutExercise(
                WorkoutExercise(
                    workoutId = workoutId,
                    exerciseId = catalog.id,
                    orderIndex = currentExercises.size
                )
            )
        }
    }

    fun updateExercise(exerciseWithCatalog: WorkoutExerciseWithCatalog, name: String, url: String, notes: String = "") {
        viewModelScope.launch {
            val catalog = repository.getCatalogExerciseById(exerciseWithCatalog.workoutExercise.exerciseId)
            if (catalog != null) {
                repository.updateCatalogExercise(catalog.copy(name = name, url = url, notes = notes))
            }
            // Update per-workout settings
            repository.updateWorkoutExercise(exerciseWithCatalog.workoutExercise)
        }
    }

    fun updateWorkoutExercise(exercise: WorkoutExercise) {
        viewModelScope.launch {
            repository.updateWorkoutExercise(exercise)
        }
    }

    fun deleteExercise(exercise: WorkoutExerciseWithCatalog) {
        viewModelScope.launch {
            repository.deleteWorkoutExercise(exercise.workoutExercise)
        }
    }

    fun duplicateExercise(exercise: WorkoutExerciseWithCatalog) {
        viewModelScope.launch {
            val allExercises = _exercises.value
            val insertIndex = allExercises.indexOfFirst { it.workoutExercise.id == exercise.workoutExercise.id } + 1

            val toShift = allExercises.filter { it.workoutExercise.orderIndex >= insertIndex }
                .map { it.workoutExercise.copy(orderIndex = it.workoutExercise.orderIndex + 1) }
            if (toShift.isNotEmpty()) repository.updateWorkoutExercises(toShift)

            repository.insertWorkoutExercise(
                exercise.workoutExercise.copy(
                    id = 0,
                    orderIndex = insertIndex
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

    fun moveExercise(fromIndex: Int, toIndex: Int) {
        val current = _exercises.value.toMutableList()
        if (fromIndex !in current.indices || toIndex !in current.indices) return
        isDragging = true
        val item = current.removeAt(fromIndex)
        current.add(toIndex, item)
        _exercises.value = current
    }

    fun commitReorder() {
        isDragging = false
        val updated = _exercises.value.mapIndexed { index, ewc ->
            ewc.workoutExercise.copy(orderIndex = index)
        }
        viewModelScope.launch {
            repository.updateWorkoutExercises(updated)
        }
    }
}
