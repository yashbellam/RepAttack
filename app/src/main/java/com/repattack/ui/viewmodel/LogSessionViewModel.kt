package com.repattack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repattack.data.model.ExerciseCatalog
import com.repattack.data.model.ExerciseLog
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
import java.util.Calendar

data class SetEntry(
    val weight: Double = 0.0,
    val reps: Int = 0,
    val completed: Boolean = false,
    val savedLogId: Long? = null // non-null = saved in DB
)

data class ExerciseLogState(
    val exerciseWithCatalog: WorkoutExerciseWithCatalog,
    val sets: List<SetEntry>
) {
    // Convenience accessors
    val workoutExercise get() = exerciseWithCatalog.workoutExercise
    val name get() = exerciseWithCatalog.name
    val url get() = exerciseWithCatalog.url
    val notes get() = exerciseWithCatalog.notes
    val exerciseId get() = exerciseWithCatalog.workoutExercise.exerciseId
}

class LogSessionViewModel(
    private val repository: RepAttackRepository,
    private val onLogChanged: (() -> Unit)? = null
) : ViewModel() {

    private val _workoutId = MutableStateFlow<Long?>(null)
    private val _workout = MutableStateFlow<Workout?>(null)
    val workout: StateFlow<Workout?> = _workout.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val exercises: StateFlow<List<WorkoutExerciseWithCatalog>> = _workoutId
        .flatMapLatest { id ->
            if (id != null) repository.getExercisesForWorkout(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _logState = MutableStateFlow<List<ExerciseLogState>>(emptyList())
    val logState: StateFlow<List<ExerciseLogState>> = _logState.asStateFlow()

    private var initialized = false
    private var dateOverrideDayMillis: Long? =
        null // midnight of overridden date, or null for today

    /** Returns the timestamp for a log entry: overridden date + current time, or just now. */
    private fun logTimestamp(): Long {
        val override = dateOverrideDayMillis ?: return System.currentTimeMillis()
        // Combine overridden date with current time-of-day
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance()
        cal.timeInMillis = override
        cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
        cal.set(Calendar.MINUTE, now.get(Calendar.MINUTE))
        cal.set(Calendar.SECOND, now.get(Calendar.SECOND))
        cal.set(Calendar.MILLISECOND, now.get(Calendar.MILLISECOND))
        return cal.timeInMillis
    }

    fun setSessionDate(millis: Long) {
        dateOverrideDayMillis = millis
    }

    fun getSessionDate(): Long = dateOverrideDayMillis ?: System.currentTimeMillis()

    /** Called when returning to the log screen — refreshes exercises if they changed. */
    fun refresh() {
        val workoutId = _workoutId.value ?: return
        viewModelScope.launch {
            _workout.value = repository.getWorkoutById(workoutId)

            val dbExercises = repository.getExercisesForWorkoutOnce(workoutId)
            val currentMap = _logState.value.associateBy { it.workoutExercise.id }

            val newLogState = dbExercises.map { ewc ->
                val existing = currentMap[ewc.workoutExercise.id]
                if (existing != null) {
                    val newNumSets = ewc.workoutExercise.targetSets ?: 3
                    val oldSets = existing.sets
                    val updatedSets = when {
                        oldSets.size < newNumSets -> oldSets + List(newNumSets - oldSets.size) { SetEntry() }
                        oldSets.size > newNumSets -> oldSets.take(newNumSets)
                        else -> oldSets
                    }
                    existing.copy(exerciseWithCatalog = ewc, sets = updatedSets)
                } else {
                    val numSets = ewc.workoutExercise.targetSets ?: 3
                    val lastLogs =
                        repository.getLastSessionForExercise(ewc.workoutExercise.exerciseId)
                    if (lastLogs.isNotEmpty()) {
                        val logsBySetNumber = lastLogs.associateBy { it.setNumber }
                        val fallbackWeight = lastLogs.mapNotNull { it.weight }.firstOrNull() ?: 0.0
                        ExerciseLogState(
                            exerciseWithCatalog = ewc,
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
                            exerciseWithCatalog = ewc,
                            sets = List(numSets) { SetEntry() })
                    }
                }
            }

            if (newLogState != _logState.value) {
                _logState.value = newLogState
            }
        }
    }

    fun loadWorkout(workoutId: Long) {
        if (initialized) return
        _workoutId.value = workoutId
        viewModelScope.launch {
            _workout.value = repository.getWorkoutById(workoutId)
        }
        viewModelScope.launch {
            exercises.collect { exerciseList ->
                if (exerciseList.isNotEmpty() && _logState.value.isEmpty()) {
                    _logState.value = exerciseList.map { ewc ->
                        val numSets = ewc.workoutExercise.targetSets ?: 3
                        val lastLogs =
                            repository.getLastSessionForExercise(ewc.workoutExercise.exerciseId)
                        if (lastLogs.isNotEmpty()) {
                            val logsBySetNumber = lastLogs.associateBy { it.setNumber }
                            val fallbackWeight =
                                lastLogs.mapNotNull { it.weight }.firstOrNull() ?: 0.0
                            ExerciseLogState(
                                exerciseWithCatalog = ewc,
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
                                exerciseWithCatalog = ewc,
                                sets = List(numSets) { SetEntry() })
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
            if (setIndex == 0) {
                for (i in 1 until sets.size) {
                    if (!sets[i].completed) sets[i] = sets[i].copy(weight = newWeight)
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
            if (setIndex == 0) {
                for (i in 1 until sets.size) {
                    if (!sets[i].completed) sets[i] =
                        sets[i].copy(weight = weight.coerceAtLeast(0.0))
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

    fun toggleSetCompleted(exerciseIndex: Int, setIndex: Int) {
        val exerciseState = _logState.value[exerciseIndex]
        val current = exerciseState.sets[setIndex]
        val nowCompleted = !current.completed

        if (nowCompleted) {
            viewModelScope.launch {
                val logId = repository.insertLog(
                    ExerciseLog(
                        exerciseId = exerciseState.exerciseId,
                        date = logTimestamp(),
                        setNumber = setIndex + 1,
                        weight = current.weight,
                        reps = current.reps
                    )
                )
                onLogChanged?.invoke()
                _logState.value = _logState.value.toMutableList().also { list ->
                    val es = list[exerciseIndex]
                    val sets = es.sets.toMutableList()
                    sets[setIndex] = current.copy(completed = true, savedLogId = logId)
                    list[exerciseIndex] = es.copy(sets = sets)
                }
            }
        } else {
            val logId = current.savedLogId
            if (logId != null) {
                viewModelScope.launch {
                    repository.deleteLogById(logId)
                    onLogChanged?.invoke()
                }
            }
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
                list[exerciseIndex] = exerciseState.copy(sets = exerciseState.sets.dropLast(1))
            }
        }
    }

    fun toggleAllSets(exerciseIndex: Int) {
        if (exerciseIndex !in _logState.value.indices) return
        val exerciseState = _logState.value[exerciseIndex]
        val allCompleted = exerciseState.sets.all { it.completed }

        if (allCompleted) {
            viewModelScope.launch {
                val savedIds = exerciseState.sets.mapNotNull { it.savedLogId }
                savedIds.forEach { repository.deleteLogById(it) }
                onLogChanged?.invoke()
                _logState.value = _logState.value.toMutableList().also { list ->
                    if (exerciseIndex in list.indices) {
                        val current = list[exerciseIndex]
                        list[exerciseIndex] = current.copy(
                            sets = current.sets.map {
                                it.copy(
                                    completed = false,
                                    savedLogId = null
                                )
                            }
                        )
                    }
                }
            }
        } else {
            viewModelScope.launch {
                val newSets = exerciseState.sets.mapIndexed { i, set ->
                    if (!set.completed) {
                        val logId = repository.insertLog(
                            ExerciseLog(
                                exerciseId = exerciseState.exerciseId,
                                date = logTimestamp(),
                                setNumber = i + 1,
                                weight = set.weight,
                                reps = set.reps
                            )
                        )
                        set.copy(completed = true, savedLogId = logId)
                    } else set
                }
                onLogChanged?.invoke()
                _logState.value = _logState.value.toMutableList().also { list ->
                    if (exerciseIndex in list.indices) {
                        list[exerciseIndex] = list[exerciseIndex].copy(sets = newSets)
                    }
                }
            }
        }
    }

    // -- Exercise CRUD (for merged screen) --

    fun addExercise(
        name: String, targetSets: Int?, minReps: Int?, maxReps: Int?,
        restTime: String, notes: String, url: String
    ) {
        val workoutId = _workoutId.value ?: return
        viewModelScope.launch {
            var catalog = repository.getCatalogExerciseByName(name)
            if (catalog == null) {
                val catalogId = repository.insertCatalogExercise(
                    ExerciseCatalog(
                        name = name,
                        url = url,
                        notes = notes
                    )
                )
                catalog = ExerciseCatalog(id = catalogId, name = name, url = url, notes = notes)
            }
            repository.insertWorkoutExercise(
                WorkoutExercise(
                    workoutId = workoutId, exerciseId = catalog.id,
                    targetSets = targetSets, minReps = minReps, maxReps = maxReps,
                    restTime = restTime,
                    orderIndex = _logState.value.size
                )
            )
            initialized = false
            _logState.value = emptyList()
            loadWorkout(workoutId)
        }
    }

    fun updateExercise(ewc: WorkoutExerciseWithCatalog, name: String, url: String) {
        viewModelScope.launch {
            val catalog = repository.getCatalogExerciseById(ewc.workoutExercise.exerciseId)
            if (catalog != null && (catalog.name != name || catalog.url != url)) {
                repository.updateCatalogExercise(catalog.copy(name = name, url = url))
            }
            repository.updateWorkoutExercise(ewc.workoutExercise)
            _logState.value = _logState.value.map { state ->
                if (state.workoutExercise.id == ewc.workoutExercise.id) state.copy(
                    exerciseWithCatalog = ewc
                )
                else state
            }
        }
    }

    fun deleteExercise(ewc: WorkoutExerciseWithCatalog) {
        viewModelScope.launch {
            repository.deleteWorkoutExercise(ewc.workoutExercise)
            _logState.value =
                _logState.value.filter { it.workoutExercise.id != ewc.workoutExercise.id }
        }
    }

    fun duplicateExercise(ewc: WorkoutExerciseWithCatalog) {
        val workoutId = _workoutId.value ?: return
        viewModelScope.launch {
            repository.insertWorkoutExercise(
                ewc.workoutExercise.copy(id = 0, orderIndex = _logState.value.size)
            )
            initialized = false
            _logState.value = emptyList()
            loadWorkout(workoutId)
        }
    }

    fun updateWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.updateWorkout(workout)
            _workout.value = workout
        }
    }
}
