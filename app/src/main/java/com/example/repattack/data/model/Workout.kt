package com.example.repattack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A workout plan — e.g. "Push Day", "Full Body", "Leg Day".
 * Contains a collection of exercises (linked via Exercise.workoutId).
 */
@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
