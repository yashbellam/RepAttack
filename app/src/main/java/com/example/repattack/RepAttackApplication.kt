package com.example.repattack

import android.app.Application
import com.example.repattack.data.RepAttackDatabase
import com.example.repattack.data.repository.RepAttackRepository
import com.example.repattack.sync.WorkoutSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class RepAttackApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: RepAttackDatabase by lazy { RepAttackDatabase.getDatabase(this) }
    val repository: RepAttackRepository by lazy {
        RepAttackRepository(
            database.workoutDao(),
            database.exerciseCatalogDao(),
            database.workoutExerciseDao(),
            database.exerciseLogDao()
        )
    }
    val syncManager: WorkoutSyncManager by lazy { WorkoutSyncManager(this, repository) }

    override fun onCreate() {
        super.onCreate()
        // Auto-sync to watch whenever workouts change
        appScope.launch {
            repository.getAllWorkouts()
                .drop(1)
                .collectLatest { syncManager.syncToWatch() }
        }
        // Auto-sync when exercises change (edits, reorder, add, delete)
        appScope.launch {
            repository.getAllCatalogExercises()
                .drop(1)
                .collectLatest { syncManager.syncToWatch() }
        }
        // Also sync on first launch (after seed data, if any)
        appScope.launch { syncManager.syncToWatch() }
    }

    /** Call after logging sets so the watch gets updated last-session data. */
    fun triggerWatchSync() {
        appScope.launch { syncManager.syncToWatch() }
    }
}
