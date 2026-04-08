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
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val exercises: List<CatalogExerciseBackup> = emptyList(),
    val workouts: List<WorkoutBackup> = emptyList(),
)

@Serializable
data class CatalogExerciseBackup(
    val name: String,
    val notes: String = "",
    val url: String = "",
    val logs: List<ExerciseLogBackup> = emptyList(),
)

@Serializable
data class WorkoutBackup(
    val name: String,
    val description: String = "",
    val createdAt: Long = 0,
    val orderIndex: Int = 0,
    val exercises: List<WorkoutExerciseBackup> = emptyList(),
)

@Serializable
data class WorkoutExerciseBackup(
    val exerciseName: String = "",
    val targetSets: Int? = null,
    val minReps: Int? = null,
    val maxReps: Int? = null,
    val restTime: String = "",
    val orderIndex: Int = 0,
)

@Serializable
data class ExerciseLogBackup(
    val date: Long,
    val setNumber: Int,
    val weight: Double? = null,
    val reps: Int? = null,
)

private val backupJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

class BackupManager(private val repository: RepAttackRepository) {

    suspend fun exportToJson(): String {
        val allCatalog = repository.getAllCatalogExercisesOnce()
        val allLogs = repository.getAllLogsOnce()
        val logsByCatalogId = allLogs.groupBy { it.exerciseId }
        val workouts = repository.getAllWorkoutsOnce()

        val backup = RepAttackBackup(
            exercises = allCatalog.map { catalog ->
                CatalogExerciseBackup(
                    name = catalog.name,
                    notes = catalog.notes,
                    url = catalog.url,
                    logs = (logsByCatalogId[catalog.id] ?: emptyList()).map { log ->
                        ExerciseLogBackup(
                            date = log.date,
                            setNumber = log.setNumber,
                            weight = log.weight,
                            reps = log.reps,
                        )
                    }
                )
            },
            workouts = workouts.map { workout ->
                val exercises = repository.getExercisesForWorkoutOnce(workout.id)
                WorkoutBackup(
                    name = workout.name,
                    description = workout.description,
                    createdAt = workout.createdAt,
                    orderIndex = workout.orderIndex,
                    exercises = exercises.map { ewc ->
                        WorkoutExerciseBackup(
                            exerciseName = ewc.name,
                            targetSets = ewc.workoutExercise.targetSets,
                            minReps = ewc.workoutExercise.minReps,
                            maxReps = ewc.workoutExercise.maxReps,
                            restTime = ewc.workoutExercise.restTime,
                            orderIndex = ewc.workoutExercise.orderIndex,
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

        // 1. Import all catalog exercises and their logs
        val catalogCache = mutableMapOf<String, Long>()

        backup.exercises.forEach { exerciseBackup ->
            val catalogId = repository.insertCatalogExercise(
                ExerciseCatalog(
                    name = exerciseBackup.name,
                    notes = exerciseBackup.notes,
                    url = exerciseBackup.url,
                )
            )
            catalogCache[exerciseBackup.name] = catalogId

            if (exerciseBackup.logs.isNotEmpty()) {
                repository.insertLogs(
                    exerciseBackup.logs.map { log ->
                        ExerciseLog(
                            exerciseId = catalogId,
                            date = log.date,
                            setNumber = log.setNumber,
                            weight = log.weight,
                            reps = log.reps,
                        )
                    }
                )
            }
        }

        // 2. Import workouts and link exercises by name
        backup.workouts.forEach { workoutBackup ->
            val workoutId = repository.insertWorkout(
                Workout(
                    name = workoutBackup.name,
                    description = workoutBackup.description,
                    createdAt = workoutBackup.createdAt,
                    orderIndex = workoutBackup.orderIndex,
                )
            )

            workoutBackup.exercises.forEach { we ->
                val catalogId = catalogCache[we.exerciseName] ?: return@forEach
                repository.insertWorkoutExercise(
                    WorkoutExercise(
                        workoutId = workoutId,
                        exerciseId = catalogId,
                        targetSets = we.targetSets,
                        minReps = we.minReps,
                        maxReps = we.maxReps,
                        restTime = we.restTime,
                        orderIndex = we.orderIndex,
                    )
                )
            }
        }
    }
}
