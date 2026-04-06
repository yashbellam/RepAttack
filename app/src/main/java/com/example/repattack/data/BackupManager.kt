package com.example.repattack.data

import android.content.Context
import android.net.Uri
import com.example.repattack.data.model.Exercise
import com.example.repattack.data.model.ExerciseLog
import com.example.repattack.data.model.Workout
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
        val exercises = repository.getAllExercisesOnce()
        val logs = repository.getAllLogsOnce()

        val logsByExercise = logs.groupBy { it.exerciseId }
        val exercisesByWorkout = exercises.groupBy { it.workoutId }

        val backup = RepAttackBackup(
            workouts = workouts.map { workout ->
                WorkoutBackup(
                    name = workout.name,
                    description = workout.description,
                    createdAt = workout.createdAt,
                    orderIndex = workout.orderIndex,
                    exercises = (exercisesByWorkout[workout.id] ?: emptyList()).map { exercise ->
                        ExerciseBackup(
                            name = exercise.name,
                            targetSets = exercise.targetSets,
                            minReps = exercise.minReps,
                            maxReps = exercise.maxReps,
                            restTime = exercise.restTime,
                            notes = exercise.notes,
                            url = exercise.url,
                            orderIndex = exercise.orderIndex,
                            logs = (logsByExercise[exercise.id] ?: emptyList()).map { log ->
                                ExerciseLogBackup(
                                    date = log.date,
                                    setNumber = log.setNumber,
                                    weight = log.weight,
                                    reps = log.reps,
                                    notes = log.notes,
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
        // Clear existing data (cascade deletes exercises and logs)
        repository.clearAllWorkouts()

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
                val exerciseId = repository.insertExercise(
                    Exercise(
                        workoutId = workoutId,
                        name = exerciseBackup.name,
                        targetSets = exerciseBackup.targetSets,
                        minReps = exerciseBackup.minReps,
                        maxReps = exerciseBackup.maxReps,
                        restTime = exerciseBackup.restTime,
                        notes = exerciseBackup.notes,
                        url = exerciseBackup.url,
                        orderIndex = exerciseBackup.orderIndex,
                    )
                )

                if (exerciseBackup.logs.isNotEmpty()) {
                    repository.insertLogs(
                        exerciseBackup.logs.map { logBackup ->
                            ExerciseLog(
                                exerciseId = exerciseId,
                                date = logBackup.date,
                                setNumber = logBackup.setNumber,
                                weight = logBackup.weight,
                                reps = logBackup.reps,
                                notes = logBackup.notes,
                            )
                        }
                    )
                }
            }
        }
    }
}
