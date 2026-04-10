package com.example.repattack.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A workout plan — e.g. "Push Day", "Full Body", "Leg Day".
 * Contains a collection of exercises (linked via WorkoutExercise).
 */
@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = Program::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("programId")]
)
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0
)
