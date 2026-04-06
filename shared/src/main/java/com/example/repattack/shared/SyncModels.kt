package com.example.repattack.shared

import kotlinx.serialization.Serializable

/**
 * Lightweight DTOs for DataLayer sync.
 * Mirrors the Room entities but keeps the shared module free of Room deps.
 */

@Serializable
data class SyncWorkout(
    val id: Long,
    val name: String,
    val description: String = "",
    val exercises: List<SyncExercise> = emptyList()
)

@Serializable
data class SyncExercise(
    val id: Long,
    val name: String,
    val targetSets: Int? = null,
    val minReps: Int? = null,
    val maxReps: Int? = null,
    val restTime: String = "",
    val notes: String = "",
    val orderIndex: Int = 0,
    val lastSets: List<SyncSet> = emptyList()
)

@Serializable
data class SyncSet(
    val setNumber: Int,
    val weight: Double? = null,
    val reps: Int? = null
)
