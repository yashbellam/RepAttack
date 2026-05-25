package com.repattack.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.repattack.data.model.ExerciseCatalog
import com.repattack.data.model.WorkoutExercise
import com.repattack.data.model.WorkoutExerciseWithCatalog
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutExerciseDao {
    /** Returns all exercises for a workout with catalog info, ordered by user's custom order. */
    @Query(
        """
        SELECT we.*, ec.name, ec.url, ec.notes 
        FROM workout_exercises we 
        JOIN exercise_catalog ec ON we.exerciseId = ec.id 
        WHERE we.workoutId = :workoutId 
        ORDER BY we.orderIndex ASC
    """
    )
    fun getByWorkoutId(workoutId: Long): Flow<List<WorkoutExerciseWithCatalog>>

    /** One-shot version. */
    @Query(
        """
        SELECT we.*, ec.name, ec.url, ec.notes 
        FROM workout_exercises we 
        JOIN exercise_catalog ec ON we.exerciseId = ec.id 
        WHERE we.workoutId = :workoutId 
        ORDER BY we.orderIndex ASC
    """
    )
    suspend fun getByWorkoutIdOnce(workoutId: Long): List<WorkoutExerciseWithCatalog>

    /** All exercises across all workouts (for stats picker — distinct catalog entries). */
    @Query(
        """
        SELECT DISTINCT ec.* FROM exercise_catalog ec
        INNER JOIN workout_exercises we ON we.exerciseId = ec.id
        ORDER BY ec.name ASC
    """
    )
    fun getAllUsedCatalogExercises(): Flow<List<ExerciseCatalog>>

    @Query("SELECT * FROM workout_exercises WHERE id = :id")
    suspend fun getById(id: Long): WorkoutExercise?

    @Query(
        """
        SELECT we.*, ec.name, ec.url, ec.notes 
        FROM workout_exercises we 
        JOIN exercise_catalog ec ON we.exerciseId = ec.id 
        WHERE we.id = :id
    """
    )
    suspend fun getByIdWithCatalog(id: Long): WorkoutExerciseWithCatalog?

    @Insert
    suspend fun insert(exercise: WorkoutExercise): Long

    @Insert
    suspend fun insertAll(exercises: List<WorkoutExercise>)

    @Update
    suspend fun update(exercise: WorkoutExercise)

    @Update
    suspend fun updateAll(exercises: List<WorkoutExercise>)

    @Delete
    suspend fun delete(exercise: WorkoutExercise)
}
