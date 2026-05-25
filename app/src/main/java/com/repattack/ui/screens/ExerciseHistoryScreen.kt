package com.repattack.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.repattack.ui.viewmodel.ExerciseHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExerciseHistoryScreen(
    exerciseId: Long,
    exerciseName: String,
    onBack: () -> Unit
) {
    val factory = remember(exerciseId) { ExerciseHistoryViewModel.Companion.factory(exerciseId) }
    val viewModel: ExerciseHistoryViewModel = viewModel(
        factory = factory,
        key = "exercise_history_$exerciseId"
    )

    val sessions by viewModel.sessions.collectAsState()
    val weightData by viewModel.weightChartData.collectAsState()
    val volumeData by viewModel.volumeChartData.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(exerciseName.ifBlank { "History" }) },
                subtitle = { Text("Stats") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior,
                collapsedHeight = 64.dp,
                navigationIcon = {
                    val haptic = LocalHapticFeedback.current
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        },
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (sessions.isEmpty() && weightData.isEmpty() && volumeData.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No sessions logged yet.\nLog this exercise to see history!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            var swipedCardDate by remember { mutableStateOf<Long?>(null) }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (weightData.size >= 2 || volumeData.size >= 2) {
                    item {
                        Text(
                            text = "Progression",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    item(key = "chart_$exerciseId") {
                        ProgressionChart(
                            weightData = weightData,
                            volumeData = volumeData
                        )
                    }
                } else if (weightData.isNotEmpty() || volumeData.isNotEmpty()) {
                    item {
                        Text(
                            text = "Log at least 2 sessions to see the chart",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (sessions.isNotEmpty()) {
                    item {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                items(sessions, key = { it.date }) { session ->
                    SessionCard(
                        session = session,
                        isSwipedOpen = session.date == swipedCardDate,
                        onSwipeStarted = { swipedCardDate = session.date },
                        onDateChangeExercise = { newDate ->
                            viewModel.updateExerciseSessionDate(session.date, newDate)
                        },
                        onDateChangeSession = { newDate ->
                            viewModel.updateSessionDate(session.date, newDate)
                        },
                        onDeleteExercise = {
                            viewModel.deleteExerciseSession(session.date)
                        },
                        onDeleteSession = {
                            viewModel.deleteSession(session.date)
                        },
                        exerciseName = exerciseName
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
