package com.repattack.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repattack.data.model.Program
import com.repattack.data.model.Workout
import com.repattack.data.model.WorkoutExercise
import com.repattack.data.repository.RepAttackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProgramViewModel(
    private val repository: RepAttackRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    val programs: StateFlow<List<Program>> =
        repository.getAllPrograms()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** All workouts, used to count per program */
    val allWorkouts: StateFlow<List<Workout>> =
        repository.getAllWorkouts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeProgramId = MutableStateFlow(prefs.getLong("active_program_id", -1L))
    val activeProgramId: StateFlow<Long> = _activeProgramId.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    fun addProgram(name: String, notes: String = "", url: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getProgramByName(name)
            if (existing != null) {
                _errorMessage.emit("\"$name\" already exists")
                return@launch
            }
            repository.insertProgram(Program(name = name, notes = notes, url = url))
        }
    }

    fun updateProgram(program: Program) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getProgramByName(program.name)
            if (existing != null && existing.id != program.id) {
                _errorMessage.emit("\"${program.name}\" already exists")
                return@launch
            }
            repository.updateProgram(program)
        }
    }

    fun deleteProgram(program: Program) {
        viewModelScope.launch(Dispatchers.IO) {
            val all = repository.getAllProgramsOnce()
            if (all.size <= 1) {
                _errorMessage.emit("Can't delete the last program")
                return@launch
            }
            repository.deleteProgram(program)
            // If we deleted the active program, switch to another
            if (_activeProgramId.value == program.id) {
                val remaining = repository.getAllProgramsOnce()
                if (remaining.isNotEmpty()) {
                    setActiveProgram(remaining.first().id)
                }
            }
        }
    }

    fun duplicateProgram(program: Program) {
        viewModelScope.launch(Dispatchers.IO) {
            var copyName = "${program.name} (copy)"
            var counter = 2
            while (repository.getProgramByName(copyName) != null) {
                copyName = "${program.name} (copy $counter)"
                counter++
            }
            val newProgramId = repository.insertProgram(
                Program(
                    name = copyName,
                    notes = program.notes,
                    url = program.url
                )
            )

            // Copy all workouts and their exercises
            val workouts = repository.getAllWorkoutsOnce().filter { it.programId == program.id }
            workouts.forEach { workout ->
                val newWorkoutId = repository.insertWorkout(
                    Workout(
                        programId = newProgramId,
                        name = workout.name,
                        description = workout.description,
                        orderIndex = workout.orderIndex
                    )
                )
                val exercises = repository.getExercisesForWorkoutOnce(workout.id)
                exercises.forEach { ewc ->
                    repository.insertWorkoutExercise(
                        WorkoutExercise(
                            workoutId = newWorkoutId,
                            exerciseId = ewc.workoutExercise.exerciseId,
                            targetSets = ewc.workoutExercise.targetSets,
                            minReps = ewc.workoutExercise.minReps,
                            maxReps = ewc.workoutExercise.maxReps,
                            restTime = ewc.workoutExercise.restTime,
                            orderIndex = ewc.workoutExercise.orderIndex,
                        )
                    )
                }
            }
        }
    }

    fun setActiveProgram(programId: Long) {
        _activeProgramId.value = programId
        prefs.edit().putLong("active_program_id", programId).apply()
    }
}
