package com.repattack.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An exercise configured within a specific workout.
 * Links a catalog exercise to a workout with per-workout settings.
 */
@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseCatalog::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutId"), Index("exerciseId")]
)
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val targetSets: Int? = null,
    val minReps: Int? = null,
    val maxReps: Int? = null,
    val restTime: String = "",
    val orderIndex: Int = 0
)
