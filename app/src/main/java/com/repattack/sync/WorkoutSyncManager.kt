package com.repattack.sync

import android.content.Context
import com.repattack.data.repository.RepAttackRepository
import com.repattack.shared.SyncExercise
import com.repattack.shared.SyncPaths
import com.repattack.shared.SyncSet
import com.repattack.shared.SyncWorkout
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Phone-side manager that pushes current workouts to the Wearable DataLayer
 * whenever the data changes. The watch picks this up via DataListenerService.
 */
class WorkoutSyncManager(
    private val context: Context,
    private val repository: RepAttackRepository
) {
    private val dataClient = Wearable.getDataClient(context)

    /** Reads workouts for the active program from Room -> converts to DTOs -> pushes via DataClient. */
    suspend fun syncToWatch() {
        val prefs = context.getSharedPreferences("repattack_prefs", Context.MODE_PRIVATE)
        val activeProgramId = prefs.getLong("active_program_id", -1L)
        val allWorkouts = repository.getAllWorkoutsOnce()
        val workouts = if (activeProgramId == -1L) allWorkouts
                       else allWorkouts.filter { it.programId == activeProgramId }
        val syncWorkouts = workouts.map { workout ->
            val exercises = repository.getExercisesForWorkoutOnce(workout.id)
            SyncWorkout(
                id = workout.id,
                name = workout.name,
                description = workout.description,
                exercises = exercises.map { ewc ->
                    val lastLogs = repository.getLastSessionForExercise(ewc.workoutExercise.exerciseId)
                    SyncExercise(
                        id = ewc.workoutExercise.exerciseId,
                        name = ewc.name,
                        targetSets = ewc.workoutExercise.targetSets,
                        minReps = ewc.workoutExercise.minReps,
                        maxReps = ewc.workoutExercise.maxReps,
                        restTime = ewc.workoutExercise.restTime,
                        notes = ewc.notes,
                        orderIndex = ewc.workoutExercise.orderIndex,
                        lastSets = lastLogs.map { log ->
                            SyncSet(
                                setNumber = log.setNumber,
                                weight = log.weight,
                                reps = log.reps
                            )
                        }
                    )
                }
            )
        }

        val json = Json.encodeToString(syncWorkouts)
        val request = PutDataMapRequest.create(SyncPaths.WORKOUTS).apply {
            dataMap.putString(SyncPaths.KEY_WORKOUTS_JSON, json)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()

        dataClient.putDataItem(request)
    }
}
