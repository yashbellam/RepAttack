package com.example.repattack.data

import android.content.Context
import android.net.Uri
import com.example.repattack.data.model.ExerciseCatalog
import com.example.repattack.data.model.ExerciseLog
import com.example.repattack.data.model.Program
import com.example.repattack.data.model.Workout
import com.example.repattack.data.model.WorkoutExercise
import com.example.repattack.data.repository.RepAttackRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class RepAttackBackup(
    val app: String,
    val version: Int,
    val exportedAt: Long = System.currentTimeMillis(),
    val programs: List<ProgramBackup> = emptyList(),
    val exercises: List<CatalogExerciseBackup> = emptyList(),
    val workouts: List<WorkoutBackup> = emptyList(),
)

@Serializable
data class ProgramBackup(
    val name: String,
    val notes: String = "",
    val url: String = "",
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
    val programName: String,
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
        val programs = repository.getAllProgramsOnce()
        val programsById = programs.associateBy { it.id }
        val workouts = repository.getAllWorkoutsOnce()

        val backup = RepAttackBackup(
            app = "RepAttack",
            version = 1,
            programs = programs.map { ProgramBackup(name = it.name, notes = it.notes, url = it.url) },
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
                    programName = programsById[workout.programId]?.name ?: "",
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
        if (backup.app != "RepAttack") {
            error("Not a RepAttack backup file.")
        }
        if (backup.version != 1) {
            error("Unsupported backup version ${backup.version}.")
        }
        if (backup.exercises.isEmpty() && backup.workouts.isEmpty()) {
            error("Backup file contains no data. Make sure this is a RepAttack backup.")
        }
        importBackup(backup)

        // Set first imported program as active
        val programs = repository.getAllProgramsOnce()
        if (programs.isNotEmpty()) {
            val prefs = context.getSharedPreferences("repattack_prefs", 0)
            prefs.edit().putLong("active_program_id", programs.first().id).apply()
        }
    }

    private suspend fun importBackup(backup: RepAttackBackup) {
        // Clear existing data
        repository.clearAllWorkouts()
        repository.clearAllPrograms()
        repository.clearAllCatalogExercises()

        // 1. Import programs
        val programCache = mutableMapOf<String, Long>()
        backup.programs.forEach { programBackup ->
            val programId = repository.insertProgram(Program(name = programBackup.name, notes = programBackup.notes, url = programBackup.url))
            programCache[programBackup.name] = programId
        }

        // 2. Import all catalog exercises and their logs
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

        // 3. Import workouts and link exercises by name
        backup.workouts.forEach { workoutBackup ->
            val programId = programCache[workoutBackup.programName] ?: return@forEach
            val workoutId = repository.insertWorkout(
                Workout(
                    programId = programId,
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
