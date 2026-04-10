package com.example.repattack.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.repattack.data.model.Program
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Query("SELECT * FROM programs ORDER BY name ASC")
    fun getAll(): Flow<List<Program>>

    @Query("SELECT * FROM programs ORDER BY name ASC")
    suspend fun getAllOnce(): List<Program>

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getById(id: Long): Program?

    @Query("SELECT * FROM programs WHERE name = :name")
    suspend fun getByName(name: String): Program?

    @Insert
    suspend fun insert(program: Program): Long

    @Update
    suspend fun update(program: Program)

    @Delete
    suspend fun delete(program: Program)

    @Query("DELETE FROM programs")
    suspend fun deleteAll()
}
