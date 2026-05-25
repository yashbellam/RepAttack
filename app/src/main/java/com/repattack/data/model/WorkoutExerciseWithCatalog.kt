package com.repattack.data.model

import androidx.room.Embedded

/**
 * A WorkoutExercise with its catalog name embedded.
 * Used by queries that join workout_exercises with exercise_catalog.
 */
data class WorkoutExerciseWithCatalog(
    @Embedded val workoutExercise: WorkoutExercise,
    val name: String,
    val url: String,
    val notes: String
)
