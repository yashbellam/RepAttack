package com.example.repattack.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.repattack.RepAttackApplication
import com.example.repattack.ui.viewmodel.LogSessionViewModel
import com.example.repattack.ui.viewmodel.WorkoutDetailViewModel
import com.example.repattack.ui.viewmodel.WorkoutListViewModel

object AppViewModelFactory {
    val Factory = viewModelFactory {
        initializer {
            WorkoutListViewModel(repAttackApplication().repository)
        }
        initializer {
            WorkoutDetailViewModel(repAttackApplication().repository)
        }
        initializer {
            LogSessionViewModel(repAttackApplication().repository)
        }
    }
}

/**
 * Helper to get the RepAttackApplication from ViewModelProvider.
 */
fun CreationExtras.repAttackApplication(): RepAttackApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RepAttackApplication)
