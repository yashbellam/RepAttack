package com.example.repattack.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.repattack.RepAttackApplication
import com.example.repattack.ui.viewmodel.ExerciseCatalogViewModel
import com.example.repattack.ui.viewmodel.LogSessionViewModel
import com.example.repattack.ui.viewmodel.ProgramViewModel
import com.example.repattack.ui.viewmodel.StatsViewModel
import com.example.repattack.ui.viewmodel.WorkoutDetailViewModel
import com.example.repattack.ui.viewmodel.WorkoutListViewModel

object AppViewModelFactory {
    val Factory = viewModelFactory {
        initializer {
            val app = repAttackApplication()
            WorkoutListViewModel(app.repository, app.getSharedPreferences("repattack_prefs", 0))
        }
        initializer {
            WorkoutDetailViewModel(repAttackApplication().repository)
        }
        initializer {
            val app = repAttackApplication()
            LogSessionViewModel(app.repository) { app.triggerWatchSync() }
        }
        initializer {
            val app = repAttackApplication()
            StatsViewModel(app.repository, app.getSharedPreferences("repattack_prefs", 0))
        }
        initializer {
            ExerciseCatalogViewModel(repAttackApplication().repository)
        }
        initializer {
            val app = repAttackApplication()
            ProgramViewModel(app.repository, app.getSharedPreferences("repattack_prefs", 0))
        }
    }
}

/**
 * Helper to get the RepAttackApplication from ViewModelProvider.
 */
fun CreationExtras.repAttackApplication(): RepAttackApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RepAttackApplication)
