package com.example.repattack.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.repattack.data.model.ExerciseLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseLogDao {
    /** Returns all logs for a catalog exercise, newest first. */
    @Query("SELECT * FROM exercise_logs WHERE exerciseId = :exerciseId ORDER BY date DESC")
    fun getByexerciseId(exerciseId: Long): Flow<List<ExerciseLog>>

    @Query("SELECT * FROM exercise_logs ORDER BY date DESC")
    suspend fun getAllOnce(): List<ExerciseLog>

    /** Returns the most recent log for each set number of a catalog exercise. */
    @Query("""
        SELECT * FROM exercise_logs e1
        WHERE exerciseId = :exerciseId
        AND date = (
            SELECT MAX(e2.date) FROM exercise_logs e2 
            WHERE e2.exerciseId = :exerciseId AND e2.setNumber = e1.setNumber
        )
        ORDER BY setNumber ASC
    """)
    suspend fun getLastSessionForExercise(exerciseId: Long): List<ExerciseLog>

    @Insert
    suspend fun insert(log: ExerciseLog): Long

    @Insert
    suspend fun insertAll(logs: List<ExerciseLog>)

    @Delete
    suspend fun delete(log: ExerciseLog)

    @Query("DELETE FROM exercise_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE exercise_logs SET date = :newDate WHERE date = :oldDate")
    suspend fun updateSessionDate(oldDate: Long, newDate: Long)
}
