package com.example.repattack.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.repattack.data.model.ExerciseLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseLogDao {
    /** Returns all logs for an exercise, newest first. */
    @Query("SELECT * FROM exercise_logs WHERE exerciseId = :exerciseId ORDER BY date DESC")
    fun getByExerciseId(exerciseId: Long): Flow<List<ExerciseLog>>

    /** Returns logs for a specific exercise on a specific date (for loading a session). */
    @Query("SELECT * FROM exercise_logs WHERE exerciseId = :exerciseId AND date >= :startOfDay AND date < :endOfDay ORDER BY setNumber ASC")
    fun getByExerciseIdAndDate(exerciseId: Long, startOfDay: Long, endOfDay: Long): Flow<List<ExerciseLog>>

    @Insert
    suspend fun insert(log: ExerciseLog): Long

    @Insert
    suspend fun insertAll(logs: List<ExerciseLog>)

    @Delete
    suspend fun delete(log: ExerciseLog)

    /** Deletes all logs for a specific exercise on a date range (re-logging a session). */
    @Query("DELETE FROM exercise_logs WHERE exerciseId = :exerciseId AND date >= :startOfDay AND date < :endOfDay")
    suspend fun deleteByExerciseIdAndDate(exerciseId: Long, startOfDay: Long, endOfDay: Long)
}
