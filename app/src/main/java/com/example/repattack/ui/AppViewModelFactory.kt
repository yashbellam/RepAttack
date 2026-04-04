package com.example.repattack.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.repattack.RepAttackApplication
import com.example.repattack.ui.viewmodel.WorkoutDetailViewModel
import com.example.repattack.ui.viewmodel.WorkoutListViewModel

/**
 * Factory that creates ViewModels with access to the repository.
 * This is manual DI — we pass the repository from the Application class.
 */
object AppViewModelFactory {
    val Factory = viewModelFactory {
        initializer {
            WorkoutListViewModel(repAttackApplication().repository)
        }
        initializer {
            WorkoutDetailViewModel(repAttackApplication().repository)
        }
    }
}

/**
 * Helper to get the RepAttackApplication from ViewModelProvider.
 */
fun CreationExtras.repAttackApplication(): RepAttackApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RepAttackApplication)
