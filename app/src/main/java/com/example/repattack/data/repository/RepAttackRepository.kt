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
    suspend fun getAllWorkoutsOnce(): List<Workout> = workoutDao.getAllOnce()
    suspend fun getWorkoutById(id: Long): Workout? = workoutDao.getById(id)
    suspend fun insertWorkout(workout: Workout): Long = workoutDao.insert(workout)
    suspend fun insertWorkouts(workouts: List<Workout>) = workoutDao.insertAll(workouts)
    suspend fun updateWorkout(workout: Workout) = workoutDao.update(workout)
    suspend fun updateWorkouts(workouts: List<Workout>) = workoutDao.updateAll(workouts)
    suspend fun deleteWorkout(workout: Workout) = workoutDao.delete(workout)
    suspend fun clearAllWorkouts() = workoutDao.deleteAll()

    // -- Exercises --
    fun getExercisesForWorkout(workoutId: Long): Flow<List<Exercise>> =
        exerciseDao.getByWorkoutId(workoutId)
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAll()
    suspend fun getAllExercisesOnce(): List<Exercise> = exerciseDao.getAllOnce()
    suspend fun getExercisesForWorkoutOnce(workoutId: Long): List<Exercise> =
        exerciseDao.getByWorkoutIdOnce(workoutId)
    suspend fun getExerciseById(id: Long): Exercise? = exerciseDao.getById(id)
    suspend fun insertExercise(exercise: Exercise): Long = exerciseDao.insert(exercise)
    suspend fun insertExercises(exercises: List<Exercise>) = exerciseDao.insertAll(exercises)
    suspend fun updateExercise(exercise: Exercise) = exerciseDao.update(exercise)
    suspend fun updateExercises(exercises: List<Exercise>) = exerciseDao.updateAll(exercises)
    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.delete(exercise)

    // -- Exercise Logs --
    fun getLogsForExercise(exerciseId: Long): Flow<List<ExerciseLog>> =
        exerciseLogDao.getByExerciseId(exerciseId)
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
