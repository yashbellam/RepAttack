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
    @Query("SELECT * FROM exercise_logs WHERE exerciseId = :exerciseId ORDER BY date DESC, setNumber ASC")
    fun getByexerciseId(exerciseId: Long): Flow<List<ExerciseLog>>

    @Query("SELECT * FROM exercise_logs ORDER BY date DESC, setNumber ASC")
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

    /** Move all logs from one day to another, preserving time-of-day. oldDate/newDate can be any timestamp on the respective days. */
    @Query("UPDATE exercise_logs SET date = date + ((:newDate / 86400000) - (:oldDate / 86400000)) * 86400000 WHERE date / 86400000 = :oldDate / 86400000")
    suspend fun updateSessionDate(oldDate: Long, newDate: Long)

    /** Move logs for a specific exercise from one day to another, preserving time-of-day. */
    @Query("UPDATE exercise_logs SET date = date + ((:newDate / 86400000) - (:oldDate / 86400000)) * 86400000 WHERE exerciseId = :exerciseId AND date / 86400000 = :oldDate / 86400000")
    suspend fun updateExerciseSessionDate(exerciseId: Long, oldDate: Long, newDate: Long)

    /** Delete all logs for a specific day. */
    @Query("DELETE FROM exercise_logs WHERE date / 86400000 = :date / 86400000")
    suspend fun deleteSessionByDate(date: Long)

    /** Delete logs for a specific exercise on a specific day. */
    @Query("DELETE FROM exercise_logs WHERE exerciseId = :exerciseId AND date / 86400000 = :date / 86400000")
    suspend fun deleteExerciseSessionByDate(exerciseId: Long, date: Long)
}
