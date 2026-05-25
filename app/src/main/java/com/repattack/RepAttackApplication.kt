package com.repattack

import android.app.Application
import android.content.SharedPreferences
import com.repattack.data.RepAttackDatabase
import com.repattack.data.model.Program
import com.repattack.data.repository.RepAttackRepository
import com.repattack.sync.WorkoutSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class RepAttackApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Held as a field to prevent GC; SharedPreferences only keeps weak refs.
    private val activeProgramListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "active_program_id") {
            triggerWatchSync()
        }
    }

    val database: RepAttackDatabase by lazy { RepAttackDatabase.Companion.getDatabase(this) }
    val repository: RepAttackRepository by lazy {
        RepAttackRepository(
            database.programDao(),
            database.workoutDao(),
            database.exerciseCatalogDao(),
            database.workoutExerciseDao(),
            database.exerciseLogDao()
        )
    }
    val syncManager: WorkoutSyncManager by lazy { WorkoutSyncManager(this, repository) }

    override fun onCreate() {
        super.onCreate()
        // Ensure a default program exists and active program is set
        appScope.launch {
            val prefs = getSharedPreferences("repattack_prefs", 0)
            val programs = repository.getAllProgramsOnce()
            if (programs.isEmpty()) {
                val id = repository.insertProgram(Program(name = "My Program"))
                prefs.edit().putLong("active_program_id", id).apply()
            } else if (prefs.getLong("active_program_id", -1L) == -1L) {
                prefs.edit().putLong("active_program_id", programs.first().id).apply()
            }
        }
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

        // Re-sync whenever the active program changes
        getSharedPreferences("repattack_prefs", 0)
            .registerOnSharedPreferenceChangeListener(activeProgramListener)
    }

    /** Call after logging sets so the watch gets updated last-session data. */
    fun triggerWatchSync() {
        appScope.launch { syncManager.syncToWatch() }
    }
}
