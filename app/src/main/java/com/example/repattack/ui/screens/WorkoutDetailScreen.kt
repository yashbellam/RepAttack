package com.example.repattack.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repattack.data.model.Exercise
import com.example.repattack.ui.AppViewModelFactory
import com.example.repattack.ui.viewmodel.WorkoutDetailViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Long,
    onBack: () -> Unit,
    viewModel: WorkoutDetailViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val workout by viewModel.workout.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var showEditWorkoutSheet by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val isScrolled = lazyListState.firstVisibleItemIndex > 0 ||
        lazyListState.firstVisibleItemScrollOffset > 0

    LaunchedEffect(workoutId) {
        viewModel.loadWorkout(workoutId)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(workout?.name ?: "Exercises") },
                subtitle = if (workout?.description?.isNotBlank() == true) {
                    { Text(workout!!.description) }
                } else null,
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
                },
                actions = {
                    val wideSize = IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide)
                    val haptic = LocalHapticFeedback.current
                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showEditWorkoutSheet = true
                        },
                        modifier = Modifier.size(wideSize),
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit workout")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        floatingActionButton = {
            SmallExtendedFloatingActionButton(
                onClick = { showAddExerciseDialog = true },
                expanded = !isScrolled,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { innerPadding ->
        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No exercises yet.\nTap + to add one!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
                viewModel.moveExercise(from.index, to.index)
            }
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(exercises.size, key = { exercises[it].id }) { index ->
                    val exercise = exercises[index]
                    ReorderableItem(reorderableLazyListState, key = exercise.id) { isDragging ->
                        LaunchedEffect(isDragging) {
                            if (!isDragging) viewModel.commitReorder()
                        }
                        ExerciseCard(
                            exercise = exercise,
                            onEdit = { exerciseToEdit = exercise },
                            onDuplicate = { viewModel.duplicateExercise(exercise) },
                            onDelete = { viewModel.deleteExercise(exercise) },
                            dragModifier = Modifier
                                .draggableHandle(
                                    onDragStarted = { haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate) }
                                )
                        )
                    }
                }
            }
        }
    }

    if (showAddExerciseDialog) {
        ExerciseEditDialog(
            exercise = null,
            onDismiss = { showAddExerciseDialog = false },
            onConfirm = { name, targetSets, minReps, maxReps, restTime, notes, url ->
                viewModel.addExercise(
                    workoutId = workoutId,
                    name = name,
                    targetSets = targetSets,
                    minReps = minReps,
                    maxReps = maxReps,
                    restTime = restTime,
                    notes = notes,
                    url = url
                )
                showAddExerciseDialog = false
            }
        )
    }

    exerciseToEdit?.let { exercise ->
        ExerciseEditDialog(
            exercise = exercise,
            onDismiss = { exerciseToEdit = null },
            onConfirm = { name, targetSets, minReps, maxReps, restTime, notes, url ->
                viewModel.updateExercise(
                    exercise.copy(
                        name = name,
                        targetSets = targetSets,
                        minReps = minReps,
                        maxReps = maxReps,
                        restTime = restTime,
                        notes = notes,
                        url = url
                    )
                )
                exerciseToEdit = null
            }
        )
    }

    if (showEditWorkoutSheet) {
        workout?.let { w ->
            WorkoutEditSheet(
                workout = w,
                onDismiss = { showEditWorkoutSheet = false },
                onConfirm = { name, description ->
                    viewModel.updateWorkout(w.copy(name = name, description = description))
                    showEditWorkoutSheet = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExerciseCard(
    exercise: Exercise,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    dragModifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surface,
                label = "swipe-bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = dragModifier
                        .fillMaxHeight()
                        .width(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⠿",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, top = 16.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    val details = buildList {
                        exercise.targetSets?.let { add("$it sets") }
                        val min = exercise.minReps
                        val max = exercise.maxReps
                        if (min != null && max != null && min != max) add("$min-$max reps")
                        else if (min != null) add("$min reps")
                        else if (max != null) add("$max reps")
                        if (exercise.restTime.isNotBlank()) add("${exercise.restTime} rest")
                    }
                    if (details.isNotEmpty()) {
                        Text(
                            text = details.joinToString(" · "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (exercise.notes.isNotBlank()) {
                        Text(
                            text = exercise.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                val narrowSize = IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Narrow)
                val buttonShapes = IconButtonDefaults.shapes()
                val haptic = LocalHapticFeedback.current
                FilledTonalIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDuplicate()
                    },
                    modifier = Modifier.size(narrowSize),
                    shapes = buttonShapes
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate")
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEdit()
                    },
                    modifier = Modifier.size(narrowSize),
                    shapes = buttonShapes
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}
