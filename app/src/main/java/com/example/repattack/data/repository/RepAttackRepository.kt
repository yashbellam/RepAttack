package com.example.repattack.data.repository

import com.example.repattack.data.dao.ExerciseDao
import com.example.repattack.data.dao.ExerciseLogDao
import com.example.repattack.data.dao.WorkoutDao
import com.example.repattack.data.model.Exercise
import com.example.repattack.data.model.ExerciseLog
import com.example.repattack.data.model.Workout
import kotlinx.coroutines.flow.Flow

class RepAttackRepository(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val exerciseLogDao: ExerciseLogDao
) {
    // -- Workouts --
    fun getAllWorkouts(): Flow<List<Workout>> = workoutDao.getAll()
    suspend fun getWorkoutById(id: Long): Workout? = workoutDao.getById(id)
    suspend fun insertWorkout(workout: Workout): Long = workoutDao.insert(workout)
    suspend fun updateWorkout(workout: Workout) = workoutDao.update(workout)
    suspend fun deleteWorkout(workout: Workout) = workoutDao.delete(workout)

    // -- Exercises --
    fun getExercisesForWorkout(workoutId: Long): Flow<List<Exercise>> =
        exerciseDao.getByWorkoutId(workoutId)
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAll()
    suspend fun getExerciseById(id: Long): Exercise? = exerciseDao.getById(id)
    suspend fun insertExercise(exercise: Exercise): Long = exerciseDao.insert(exercise)
    suspend fun updateExercise(exercise: Exercise) = exerciseDao.update(exercise)
    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.delete(exercise)

    // -- Exercise Logs --
    fun getLogsForExercise(exerciseId: Long): Flow<List<ExerciseLog>> =
        exerciseLogDao.getByExerciseId(exerciseId)
    suspend fun getLastSessionForExercise(exerciseId: Long): List<ExerciseLog> =
        exerciseLogDao.getLastSessionForExercise(exerciseId)
    suspend fun insertLog(log: ExerciseLog): Long = exerciseLogDao.insert(log)
    suspend fun insertLogs(logs: List<ExerciseLog>) = exerciseLogDao.insertAll(logs)
    suspend fun deleteLog(log: ExerciseLog) = exerciseLogDao.delete(log)
    suspend fun deleteLogById(id: Long) = exerciseLogDao.deleteById(id)
}
