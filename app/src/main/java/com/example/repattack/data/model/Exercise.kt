package com.example.repattack.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An exercise within a workout — e.g. "Bench Press".
 * Linked to a Workout via workoutId foreign key.
 *
 * Most fields are optional targets — the user fills in actuals during logging.
 */
@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutId")]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val name: String,
    val targetSets: Int? = null,
    val targetReps: Int? = null,
    val restTimeSeconds: Int? = null,
    val notes: String = "",
    val url: String = "",
    val orderIndex: Int = 0
)
