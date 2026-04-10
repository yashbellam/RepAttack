package com.example.repattack.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.repattack.data.model.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY orderIndex ASC")
    fun getAll(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE programId = :programId ORDER BY orderIndex ASC")
    fun getByProgramId(programId: Long): Flow<List<Workout>>

    @Query("SELECT * FROM workouts ORDER BY orderIndex ASC")
    suspend fun getAllOnce(): List<Workout>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: Long): Workout?

    @Insert
    suspend fun insert(workout: Workout): Long

    @Insert
    suspend fun insertAll(workouts: List<Workout>)

    @Update
    suspend fun update(workout: Workout)

    @Update
    suspend fun updateAll(workouts: List<Workout>)

    @Delete
    suspend fun delete(workout: Workout)

    @Query("DELETE FROM workouts")
    suspend fun deleteAll()
}
