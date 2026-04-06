package com.example.repattack

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.repattack.data.BackupManager
import com.example.repattack.data.RepAttackDatabase
import com.example.repattack.data.repository.RepAttackRepository
import com.example.repattack.navigation.Screen
import com.example.repattack.ui.screens.SettingsScreen
import com.example.repattack.ui.screens.StatsScreen
import com.example.repattack.ui.screens.WorkoutsScreen
import com.example.repattack.ui.theme.RepAttackTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class TopLevelRoute(val label: String, val route: Screen, val icon: ImageVector)

val topLevelRoutes = listOf(
    TopLevelRoute("Workouts", Screen.Workouts, Icons.Filled.FitnessCenter),
    TopLevelRoute("Stats", Screen.Stats, Icons.Filled.BarChart),
    TopLevelRoute("Settings", Screen.Settings, Icons.Filled.Settings),
)

/** Returns the tab index for a destination, or -1 if not a tab. */
private fun tabIndex(dest: androidx.navigation.NavDestination?): Int {
    if (dest == null) return -1
    return topLevelRoutes.indexOfFirst { dest.hasRoute(it.route::class) }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class MainActivity : ComponentActivity() {

    private val backupManager by lazy {
        val db = RepAttackDatabase.getDatabase(this)
        BackupManager(RepAttackRepository(db.workoutDao(), db.exerciseDao(), db.exerciseLogDao()))
    }

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@registerForActivityResult
        CoroutineScope(Dispatchers.IO).launch {
            try {
                backupManager.exportToUri(this@MainActivity, uri)
                runOnUiThread { Toast.makeText(this@MainActivity, "Data exported", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this@MainActivity, "Export failed: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        CoroutineScope(Dispatchers.IO).launch {
            try {
                backupManager.importFromUri(this@MainActivity, uri)
                runOnUiThread { Toast.makeText(this@MainActivity, "Data imported", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this@MainActivity, "Import failed: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
    }

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
                        ShortNavigationBar {
                            topLevelRoutes.forEach { route ->
                                ShortNavigationBarItem(
                                    icon = { Icon(route.icon, contentDescription = route.label) },
                                    label = { Text(route.label) },
                                    selected = currentDestination?.hasRoute(route.route::class) == true,
                                    onClick = {
                                        navController.navigate(route.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
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
                            .padding(bottom = innerPadding.calculateBottomPadding()),
                        enterTransition = {
                            val from = tabIndex(initialState.destination)
                            val to = tabIndex(targetState.destination)
                            val direction = if (to > from)
                                AnimatedContentTransitionScope.SlideDirection.Start
                            else
                                AnimatedContentTransitionScope.SlideDirection.End
                            slideIntoContainer(direction, tween(350))
                        },
                        exitTransition = {
                            val from = tabIndex(initialState.destination)
                            val to = tabIndex(targetState.destination)
                            val direction = if (to > from)
                                AnimatedContentTransitionScope.SlideDirection.Start
                            else
                                AnimatedContentTransitionScope.SlideDirection.End
                            slideOutOfContainer(direction, tween(350))
                        },
                        popEnterTransition = {
                            val from = tabIndex(initialState.destination)
                            val to = tabIndex(targetState.destination)
                            val direction = if (to > from)
                                AnimatedContentTransitionScope.SlideDirection.Start
                            else
                                AnimatedContentTransitionScope.SlideDirection.End
                            slideIntoContainer(direction, tween(350))
                        },
                        popExitTransition = {
                            val from = tabIndex(initialState.destination)
                            val to = tabIndex(targetState.destination)
                            val direction = if (to > from)
                                AnimatedContentTransitionScope.SlideDirection.Start
                            else
                                AnimatedContentTransitionScope.SlideDirection.End
                            slideOutOfContainer(direction, tween(350))
                        },
                    ) {
                        composable<Screen.Workouts> {
                            WorkoutsScreen(
                                onWorkoutClick = { workoutId ->
                                    startActivity(
                                        LogSessionActivity.newIntent(this@MainActivity, workoutId)
                                    )
                                }
                            )
                        }

                        composable<Screen.Stats> {
                            BackHandler {
                                navController.navigate(Screen.Workouts) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            StatsScreen()
                        }

                        composable<Screen.Settings> {
                            BackHandler {
                                navController.navigate(Screen.Workouts) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            SettingsScreen(
                                onExport = {
                                    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                                    exportLauncher.launch("repattack_backup_$timestamp.json")
                                },
                                onImport = {
                                    importLauncher.launch(arrayOf("application/json"))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
