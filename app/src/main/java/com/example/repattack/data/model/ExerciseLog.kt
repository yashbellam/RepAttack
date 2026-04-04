package com.example.repattack.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single logged set — e.g. "Bench Press, set 2, 135 lbs, 8 reps".
 * Created during a workout session.
 */
@Entity(
    tableName = "exercise_logs",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId")]
)
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val date: Long = System.currentTimeMillis(),
    val setNumber: Int,
    val weight: Double? = null,
    val reps: Int? = null,
    val notes: String = ""
)
