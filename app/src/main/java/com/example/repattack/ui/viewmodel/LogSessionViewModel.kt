package com.example.repattack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repattack.data.model.Exercise
import com.example.repattack.data.model.ExerciseLog
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

data class SetEntry(
    val weight: Double = 0.0,
    val reps: Int = 0,
    val completed: Boolean = false,
    val savedLogId: Long? = null // non-null = saved in DB
)

data class ExerciseLogState(
    val exercise: Exercise,
    val sets: List<SetEntry>
)

class LogSessionViewModel(
    private val repository: RepAttackRepository
) : ViewModel() {

    private val _workoutId = MutableStateFlow<Long?>(null)
    private val _workout = MutableStateFlow<Workout?>(null)
    val workout: StateFlow<Workout?> = _workout.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val exercises: StateFlow<List<Exercise>> = _workoutId
        .flatMapLatest { id ->
            if (id != null) repository.getExercisesForWorkout(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _logState = MutableStateFlow<List<ExerciseLogState>>(emptyList())
    val logState: StateFlow<List<ExerciseLogState>> = _logState.asStateFlow()

    private var initialized = false
    private var sessionTimestamp: Long = 0L

    fun loadWorkout(workoutId: Long) {
        if (initialized) return
        _workoutId.value = workoutId
        sessionTimestamp = System.currentTimeMillis()
        viewModelScope.launch {
            _workout.value = repository.getWorkoutById(workoutId)
        }
        viewModelScope.launch {
            exercises.collect { exerciseList ->
                if (exerciseList.isNotEmpty() && _logState.value.isEmpty()) {
                    _logState.value = exerciseList.map { exercise ->
                        val numSets = exercise.targetSets ?: 3
                        val lastLogs = repository.getLastSessionForExercise(exercise.id)
                        if (lastLogs.isNotEmpty()) {
                            // Pre-fill from last session, matching by set number
                            val logsBySetNumber = lastLogs.associateBy { it.setNumber }
                            // Use the most common weight as fallback
                            val fallbackWeight = lastLogs.mapNotNull { it.weight }.firstOrNull() ?: 0.0
                            ExerciseLogState(
                                exercise = exercise,
                                sets = List(numSets) { index ->
                                    val log = logsBySetNumber[index + 1]
                                    SetEntry(
                                        weight = log?.weight ?: fallbackWeight,
                                        reps = log?.reps ?: 0
                                    )
                                }
                            )
                        } else {
                            ExerciseLogState(
                                exercise = exercise,
                                sets = List(numSets) { SetEntry() }
                            )
                        }
                    }
                    initialized = true
                }
            }
        }
    }

    fun updateWeight(exerciseIndex: Int, setIndex: Int, delta: Double) {
        _logState.value = _logState.value.toMutableList().also { list ->
            val exerciseState = list[exerciseIndex]
            val sets = exerciseState.sets.toMutableList()
            val current = sets[setIndex]
            val newWeight = (current.weight + delta).coerceAtLeast(0.0)
            sets[setIndex] = current.copy(weight = newWeight)
            // If first set changed, propagate to other non-completed sets
            if (setIndex == 0) {
                for (i in 1 until sets.size) {
                    if (!sets[i].completed) {
                        sets[i] = sets[i].copy(weight = newWeight)
                    }
                }
            }
            list[exerciseIndex] = exerciseState.copy(sets = sets)
        }
    }

    fun setWeight(exerciseIndex: Int, setIndex: Int, weight: Double) {
        _logState.value = _logState.value.toMutableList().also { list ->
            val exerciseState = list[exerciseIndex]
            val sets = exerciseState.sets.toMutableList()
            sets[setIndex] = sets[setIndex].copy(weight = weight.coerceAtLeast(0.0))
            // If first set changed, propagate to other non-completed sets
            if (setIndex == 0) {
                for (i in 1 until sets.size) {
                    if (!sets[i].completed) {
                        sets[i] = sets[i].copy(weight = weight.coerceAtLeast(0.0))
                    }
                }
            }
            list[exerciseIndex] = exerciseState.copy(sets = sets)
        }
    }

    fun updateReps(exerciseIndex: Int, setIndex: Int, delta: Int) {
        _logState.value = _logState.value.toMutableList().also { list ->
            val exerciseState = list[exerciseIndex]
            val sets = exerciseState.sets.toMutableList()
            val current = sets[setIndex]
            sets[setIndex] = current.copy(reps = (current.reps + delta).coerceAtLeast(0))
            list[exerciseIndex] = exerciseState.copy(sets = sets)
        }
    }

    fun setReps(exerciseIndex: Int, setIndex: Int, reps: Int) {
        _logState.value = _logState.value.toMutableList().also { list ->
            val exerciseState = list[exerciseIndex]
            val sets = exerciseState.sets.toMutableList()
            sets[setIndex] = sets[setIndex].copy(reps = reps.coerceAtLeast(0))
            list[exerciseIndex] = exerciseState.copy(sets = sets)
        }
    }

    /** Saves or deletes a set from the DB when the checkmark is toggled. */
    fun toggleSetCompleted(exerciseIndex: Int, setIndex: Int) {
        val exerciseState = _logState.value[exerciseIndex]
        val current = exerciseState.sets[setIndex]
        val nowCompleted = !current.completed

        if (nowCompleted) {
            // Save to DB
            viewModelScope.launch {
                val logId = repository.insertLog(
                    ExerciseLog(
                        exerciseId = exerciseState.exercise.id,
                        date = sessionTimestamp,
                        setNumber = setIndex + 1,
                        weight = current.weight,
                        reps = current.reps
                    )
                )
                // Update state with saved ID
                _logState.value = _logState.value.toMutableList().also { list ->
                    val es = list[exerciseIndex]
                    val sets = es.sets.toMutableList()
                    sets[setIndex] = current.copy(completed = true, savedLogId = logId)
                    list[exerciseIndex] = es.copy(sets = sets)
                }
            }
        } else {
            // Delete from DB
            val logId = current.savedLogId
            if (logId != null) {
                viewModelScope.launch {
                    repository.deleteLogById(logId)
                }
            }
            // Update state
            _logState.value = _logState.value.toMutableList().also { list ->
                val es = list[exerciseIndex]
                val sets = es.sets.toMutableList()
                sets[setIndex] = current.copy(completed = false, savedLogId = null)
                list[exerciseIndex] = es.copy(sets = sets)
            }
        }
    }

    fun addSet(exerciseIndex: Int) {
        _logState.value = _logState.value.toMutableList().also { list ->
            val exerciseState = list[exerciseIndex]
            val lastSet = exerciseState.sets.lastOrNull() ?: SetEntry()
            list[exerciseIndex] = exerciseState.copy(
                sets = exerciseState.sets + SetEntry(weight = lastSet.weight, reps = lastSet.reps)
            )
        }
    }

    fun removeSet(exerciseIndex: Int) {
        _logState.value = _logState.value.toMutableList().also { list ->
            val exerciseState = list[exerciseIndex]
            if (exerciseState.sets.size > 1) {
                list[exerciseIndex] = exerciseState.copy(
                    sets = exerciseState.sets.dropLast(1)
                )
            }
        }
    }

    /** Check or uncheck all sets for an exercise at once. */
    fun toggleAllSets(exerciseIndex: Int) {
        if (exerciseIndex !in _logState.value.indices) return
        val exerciseState = _logState.value[exerciseIndex]
        val allCompleted = exerciseState.sets.all { it.completed }

        if (allCompleted) {
            // Uncheck all — delete from DB and update state
            viewModelScope.launch {
                val savedIds = exerciseState.sets.mapNotNull { it.savedLogId }
                savedIds.forEach { repository.deleteLogById(it) }
                _logState.value = _logState.value.toMutableList().also { list ->
                    if (exerciseIndex in list.indices) {
                        val current = list[exerciseIndex]
                        list[exerciseIndex] = current.copy(
                            sets = current.sets.map { it.copy(completed = false, savedLogId = null) }
                        )
                    }
                }
            }
        } else {
            // Check all unchecked — save to DB and update state
            viewModelScope.launch {
                val newSets = exerciseState.sets.mapIndexed { i, set ->
                    if (!set.completed) {
                        val logId = repository.insertLog(
                            ExerciseLog(
                                exerciseId = exerciseState.exercise.id,
                                date = sessionTimestamp,
                                setNumber = i + 1,
                                weight = set.weight,
                                reps = set.reps
                            )
                        )
                        set.copy(completed = true, savedLogId = logId)
                    } else set
                }
                _logState.value = _logState.value.toMutableList().also { list ->
                    if (exerciseIndex in list.indices) {
                        list[exerciseIndex] = list[exerciseIndex].copy(sets = newSets)
                    }
                }
            }
        }
    }
}
