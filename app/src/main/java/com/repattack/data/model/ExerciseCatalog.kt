package com.repattack.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A global exercise definition — e.g. "Bench Press".
 * Shared across all workouts. History (logs) links here.
 */
@Entity(
    tableName = "exercise_catalog",
    indices = [Index(value = ["name"], unique = true)]
)
data class ExerciseCatalog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String = "",
    val notes: String = ""
)
