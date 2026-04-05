package com.example.repattack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.repattack.navigation.Screen
import com.example.repattack.ui.screens.LogSessionScreen
import com.example.repattack.ui.screens.StatsScreen
import com.example.repattack.ui.screens.WorkoutDetailScreen
import com.example.repattack.ui.screens.WorkoutsScreen
import com.example.repattack.ui.theme.RepAttackTheme

/** Represents a tab in the bottom navigation bar. */
data class TopLevelRoute(val label: String, val route: Screen, val icon: ImageVector)

val topLevelRoutes = listOf(
    TopLevelRoute("Workouts", Screen.Workouts, Icons.Filled.FitnessCenter),
    TopLevelRoute("Stats", Screen.Stats, Icons.Filled.BarChart),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RepAttackTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = currentBackStackEntry?.destination

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            topLevelRoutes.forEach { route ->
                                NavigationBarItem(
                                    icon = { Icon(route.icon, contentDescription = route.label) },
                                    label = { Text(route.label) },
                                    selected = currentDestination?.hasRoute(route.route::class) == true,
                                    onClick = {
                                        navController.navigate(route.route) {
                                            // Pop up to the start destination to avoid
                                            // building up a large back stack
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            // Avoid multiple copies of the same destination
                                            launchSingleTop = true
                                            // Restore state when re-selecting a tab
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Workouts,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable<Screen.Workouts> {
                            WorkoutsScreen(
                                onWorkoutClick = { workoutId ->
                                    navController.navigate(Screen.LogSession(workoutId))
                                }
                            )
                        }
                        composable<Screen.Stats> { StatsScreen() }
                        composable<Screen.LogSession> { backStackEntry ->
                            val logSession = backStackEntry.toRoute<Screen.LogSession>()
                            LogSessionScreen(
                                workoutId = logSession.workoutId,
                                onBack = {
                                    navController.previousBackStackEntry?.let {
                                        navController.popBackStack()
                                    }
                                },
                                onEditExercises = { workoutId ->
                                    navController.navigate(Screen.WorkoutDetail(workoutId))
                                }
                            )
                        }
                        composable<Screen.WorkoutDetail> { backStackEntry ->
                            val workoutDetail = backStackEntry.toRoute<Screen.WorkoutDetail>()
                            WorkoutDetailScreen(
                                workoutId = workoutDetail.workoutId,
                                onBack = {
                                    navController.previousBackStackEntry?.let {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
