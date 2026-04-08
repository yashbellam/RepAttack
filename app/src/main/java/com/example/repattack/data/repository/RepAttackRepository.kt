package com.example.repattack.data.repository

import com.example.repattack.data.dao.ExerciseCatalogDao
import com.example.repattack.data.dao.ExerciseLogDao
import com.example.repattack.data.dao.WorkoutDao
import com.example.repattack.data.dao.WorkoutExerciseDao
import com.example.repattack.data.model.ExerciseCatalog
import com.example.repattack.data.model.ExerciseLog
import com.example.repattack.data.model.Workout
import com.example.repattack.data.model.WorkoutExercise
import com.example.repattack.data.model.WorkoutExerciseWithCatalog
import kotlinx.coroutines.flow.Flow

class RepAttackRepository(
    private val workoutDao: WorkoutDao,
    private val catalogDao: ExerciseCatalogDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val exerciseLogDao: ExerciseLogDao
) {
    // -- Workouts --
    fun getAllWorkouts(): Flow<List<Workout>> = workoutDao.getAll()
    suspend fun getAllWorkoutsOnce(): List<Workout> = workoutDao.getAllOnce()
    suspend fun getWorkoutById(id: Long): Workout? = workoutDao.getById(id)
    suspend fun insertWorkout(workout: Workout): Long = workoutDao.insert(workout)
    suspend fun insertWorkouts(workouts: List<Workout>) = workoutDao.insertAll(workouts)
    suspend fun updateWorkout(workout: Workout) = workoutDao.update(workout)
    suspend fun updateWorkouts(workouts: List<Workout>) = workoutDao.updateAll(workouts)
    suspend fun deleteWorkout(workout: Workout) = workoutDao.delete(workout)
    suspend fun clearAllWorkouts() = workoutDao.deleteAll()

    // -- Exercise Catalog --
    fun getAllCatalogExercises(): Flow<List<ExerciseCatalog>> = catalogDao.getAll()
    suspend fun getAllCatalogExercisesOnce(): List<ExerciseCatalog> = catalogDao.getAllOnce()
    suspend fun getCatalogExerciseById(id: Long): ExerciseCatalog? = catalogDao.getById(id)
    suspend fun getCatalogExerciseByName(name: String): ExerciseCatalog? = catalogDao.getByName(name)
    suspend fun insertCatalogExercise(exercise: ExerciseCatalog): Long = catalogDao.insert(exercise)
    suspend fun insertCatalogExercises(exercises: List<ExerciseCatalog>) = catalogDao.insertAll(exercises)
    suspend fun updateCatalogExercise(exercise: ExerciseCatalog) = catalogDao.update(exercise)
    suspend fun deleteCatalogExercise(exercise: ExerciseCatalog) = catalogDao.delete(exercise)
    suspend fun clearAllCatalogExercises() = catalogDao.deleteAll()

    // -- Workout Exercises --
    fun getExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExerciseWithCatalog>> =
        workoutExerciseDao.getByWorkoutId(workoutId)
    suspend fun getExercisesForWorkoutOnce(workoutId: Long): List<WorkoutExerciseWithCatalog> =
        workoutExerciseDao.getByWorkoutIdOnce(workoutId)
    fun getAllUsedExercises(): Flow<List<ExerciseCatalog>> =
        workoutExerciseDao.getAllUsedCatalogExercises()
    suspend fun getWorkoutExerciseById(id: Long): WorkoutExercise? = workoutExerciseDao.getById(id)
    suspend fun insertWorkoutExercise(exercise: WorkoutExercise): Long = workoutExerciseDao.insert(exercise)
    suspend fun insertWorkoutExercises(exercises: List<WorkoutExercise>) = workoutExerciseDao.insertAll(exercises)
    suspend fun updateWorkoutExercise(exercise: WorkoutExercise) = workoutExerciseDao.update(exercise)
    suspend fun updateWorkoutExercises(exercises: List<WorkoutExercise>) = workoutExerciseDao.updateAll(exercises)
    suspend fun deleteWorkoutExercise(exercise: WorkoutExercise) = workoutExerciseDao.delete(exercise)

    // -- Exercise Logs --
    fun getLogsForExercise(exerciseId: Long): Flow<List<ExerciseLog>> =
        exerciseLogDao.getByexerciseId(exerciseId)
    suspend fun getAllLogsOnce(): List<ExerciseLog> = exerciseLogDao.getAllOnce()
    suspend fun getLastSessionForExercise(exerciseId: Long): List<ExerciseLog> =
        exerciseLogDao.getLastSessionForExercise(exerciseId)
    suspend fun insertLog(log: ExerciseLog): Long = exerciseLogDao.insert(log)
    suspend fun insertLogs(logs: List<ExerciseLog>) = exerciseLogDao.insertAll(logs)
    suspend fun deleteLog(log: ExerciseLog) = exerciseLogDao.delete(log)
    suspend fun deleteLogById(id: Long) = exerciseLogDao.deleteById(id)
    suspend fun updateSessionDate(oldDate: Long, newDate: Long) =
        exerciseLogDao.updateSessionDate(oldDate, newDate)
}
