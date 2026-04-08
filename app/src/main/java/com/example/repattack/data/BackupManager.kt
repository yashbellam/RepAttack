package com.example.repattack.data

import android.content.Context
import android.net.Uri
import com.example.repattack.data.model.ExerciseCatalog
import com.example.repattack.data.model.ExerciseLog
import com.example.repattack.data.model.Workout
import com.example.repattack.data.model.WorkoutExercise
import com.example.repattack.data.repository.RepAttackRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class RepAttackBackup(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val workouts: List<WorkoutBackup>,
)

@Serializable
data class WorkoutBackup(
    val name: String,
    val description: String = "",
    val createdAt: Long = 0,
    val orderIndex: Int = 0,
    val exercises: List<ExerciseBackup> = emptyList(),
)

@Serializable
data class ExerciseBackup(
    val name: String,
    val targetSets: Int? = null,
    val minReps: Int? = null,
    val maxReps: Int? = null,
    val restTime: String = "",
    val notes: String = "",
    val url: String = "",
    val orderIndex: Int = 0,
    val logs: List<ExerciseLogBackup> = emptyList(),
)

@Serializable
data class ExerciseLogBackup(
    val date: Long,
    val setNumber: Int,
    val weight: Double? = null,
    val reps: Int? = null,
    val notes: String = "",
)

private val backupJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

class BackupManager(private val repository: RepAttackRepository) {

    suspend fun exportToJson(): String {
        val workouts = repository.getAllWorkoutsOnce()
        val logs = repository.getAllLogsOnce()
        val logsByCatalogId = logs.groupBy { it.exerciseId }

        val backup = RepAttackBackup(
            workouts = workouts.map { workout ->
                val exercises = repository.getExercisesForWorkoutOnce(workout.id)
                WorkoutBackup(
                    name = workout.name,
                    description = workout.description,
                    createdAt = workout.createdAt,
                    orderIndex = workout.orderIndex,
                    exercises = exercises.map { ewc ->
                        ExerciseBackup(
                            name = ewc.name,
                            targetSets = ewc.workoutExercise.targetSets,
                            minReps = ewc.workoutExercise.minReps,
                            maxReps = ewc.workoutExercise.maxReps,
                            restTime = ewc.workoutExercise.restTime,
                            notes = ewc.notes,
                            url = ewc.url,
                            orderIndex = ewc.workoutExercise.orderIndex,
                            logs = (logsByCatalogId[ewc.workoutExercise.exerciseId] ?: emptyList()).map { log ->
                                ExerciseLogBackup(
                                    date = log.date,
                                    setNumber = log.setNumber,
                                    weight = log.weight,
                                    reps = log.reps,
                                )
                            }
                        )
                    }
                )
            }
        )

        return backupJson.encodeToString(backup)
    }

    suspend fun exportToUri(context: Context, uri: Uri) {
        val json = exportToJson()
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(json.toByteArray(Charsets.UTF_8))
        }
    }

    suspend fun importFromUri(context: Context, uri: Uri) {
        val json = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().readText()
        } ?: return

        val backup = backupJson.decodeFromString<RepAttackBackup>(json)
        importBackup(backup)
    }

    private suspend fun importBackup(backup: RepAttackBackup) {
        // Clear existing data
        repository.clearAllWorkouts()
        repository.clearAllCatalogExercises()

        // Track catalog exercises by name to deduplicate
        val catalogCache = mutableMapOf<String, Long>()

        backup.workouts.forEach { workoutBackup ->
            val workoutId = repository.insertWorkout(
                Workout(
                    name = workoutBackup.name,
                    description = workoutBackup.description,
                    createdAt = workoutBackup.createdAt,
                    orderIndex = workoutBackup.orderIndex,
                )
            )

            workoutBackup.exercises.forEach { exerciseBackup ->
                // Find or create catalog entry
                val catalogId = catalogCache.getOrPut(exerciseBackup.name) {
                    repository.insertCatalogExercise(
                        ExerciseCatalog(
                            name = exerciseBackup.name,
                            url = exerciseBackup.url,
                            notes = exerciseBackup.notes
                        )
                    )
                }

                repository.insertWorkoutExercise(
                    WorkoutExercise(
                        workoutId = workoutId,
                        exerciseId = catalogId,
                        targetSets = exerciseBackup.targetSets,
                        minReps = exerciseBackup.minReps,
                        maxReps = exerciseBackup.maxReps,
                        restTime = exerciseBackup.restTime,
                        orderIndex = exerciseBackup.orderIndex,
                    )
                )

                if (exerciseBackup.logs.isNotEmpty()) {
                    repository.insertLogs(
                        exerciseBackup.logs.map { logBackup ->
                            ExerciseLog(
                                exerciseId = catalogId,
                                date = logBackup.date,
                                setNumber = logBackup.setNumber,
                                weight = logBackup.weight,
                                reps = logBackup.reps,
                            )
                        }
                    )
                }
            }
        }
    }
}
