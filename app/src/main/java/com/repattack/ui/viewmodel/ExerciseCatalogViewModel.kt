package com.repattack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repattack.data.model.ExerciseCatalog
import com.repattack.data.repository.RepAttackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExerciseCatalogViewModel(
    private val repository: RepAttackRepository
) : ViewModel() {

    val exercises: StateFlow<List<ExerciseCatalog>> =
        repository.getAllCatalogExercises()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    fun addExercise(name: String, notes: String, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getCatalogExerciseByName(name)
            if (existing != null) {
                _errorMessage.emit("\"$name\" already exists")
                return@launch
            }
            repository.insertCatalogExercise(
                ExerciseCatalog(name = name, notes = notes, url = url)
            )
        }
    }

    fun updateExercise(exercise: ExerciseCatalog) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getCatalogExerciseByName(exercise.name)
            if (existing != null && existing.id != exercise.id) {
                _errorMessage.emit("\"${exercise.name}\" already exists")
                return@launch
            }
            repository.updateCatalogExercise(exercise)
        }
    }

    fun duplicateExercise(exercise: ExerciseCatalog) {
        viewModelScope.launch(Dispatchers.IO) {
            var copyName = "${exercise.name} (copy)"
            var counter = 2
            while (repository.getCatalogExerciseByName(copyName) != null) {
                copyName = "${exercise.name} (copy $counter)"
                counter++
            }
            repository.insertCatalogExercise(
                exercise.copy(id = 0, name = copyName)
            )
        }
    }

    fun deleteExercise(exercise: ExerciseCatalog) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCatalogExercise(exercise)
        }
    }
}
