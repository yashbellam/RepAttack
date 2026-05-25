package com.repattack.wear.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.repattack.wear.data.WearWorkoutRepository

@Composable
fun WearNavHost(repository: WearWorkoutRepository) {
    val navController = rememberSwipeDismissableNavController()
    val workouts by repository.workouts.collectAsState()

    AppScaffold {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "workout_list"
        ) {
            composable("workout_list") {
                WorkoutListScreen(
                    workouts = workouts,
                    onWorkoutClick = { workoutId ->
                        navController.navigate("workout_detail/$workoutId")
                    }
                )
            }
            composable("workout_detail/{workoutId}") { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getString("workoutId")?.toLongOrNull()
                val workout = workouts.find { it.id == workoutId }
                if (workout != null) {
                    WorkoutDetailScreen(workout = workout)
                }
            }
        }
    }
}
