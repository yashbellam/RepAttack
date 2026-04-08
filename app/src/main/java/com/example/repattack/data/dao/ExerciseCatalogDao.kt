package com.example.repattack.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.repattack.data.model.ExerciseCatalog
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseCatalogDao {
    @Query("SELECT * FROM exercise_catalog ORDER BY name ASC")
    fun getAll(): Flow<List<ExerciseCatalog>>

    @Query("SELECT * FROM exercise_catalog ORDER BY name ASC")
    suspend fun getAllOnce(): List<ExerciseCatalog>

    @Query("SELECT * FROM exercise_catalog WHERE id = :id")
    suspend fun getById(id: Long): ExerciseCatalog?

    @Query("SELECT * FROM exercise_catalog WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): ExerciseCatalog?

    @Insert
    suspend fun insert(exercise: ExerciseCatalog): Long

    @Insert
    suspend fun insertAll(exercises: List<ExerciseCatalog>)

    @Update
    suspend fun update(exercise: ExerciseCatalog)

    @Delete
    suspend fun delete(exercise: ExerciseCatalog)

    @Query("DELETE FROM exercise_catalog")
    suspend fun deleteAll()
}
