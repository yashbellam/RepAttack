package com.repattack.navigation

import kotlinx.serialization.Serializable

/**
 * Defines all top-level navigation destinations.
 * Each object is a unique route that NavHost uses to identify screens.
 */
sealed interface Screen {
    @Serializable
    data object Workouts : Screen
    @Serializable
    data object Log : Screen
    @Serializable
    data object Stats : Screen
    @Serializable
    data object Settings : Screen
    @Serializable
    data class WorkoutDetail(val workoutId: Long) : Screen
    @Serializable
    data class LogSession(val workoutId: Long) : Screen
}
