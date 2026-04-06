package com.example.repattack.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.example.repattack.data.model.Workout
import com.example.repattack.ui.AppViewModelFactory
import com.example.repattack.ui.viewmodel.WorkoutListViewModel
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WorkoutsScreen(
    onWorkoutClick: (Long) -> Unit,
    viewModel: WorkoutListViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val workouts by viewModel.workouts.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var workoutToEdit by remember { mutableStateOf<Workout?>(null) }
    var lastClickTime by remember { mutableStateOf(0L) }

    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val isScrolled = lazyListState.firstVisibleItemIndex > 0 ||
        lazyListState.firstVisibleItemScrollOffset > 0

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Rep Attack") },
                subtitle = { Text("Your workouts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                scrollBehavior = scrollBehavior,
                collapsedHeight = 64.dp
            )
        },
        floatingActionButton = {
            SmallExtendedFloatingActionButton(
                onClick = {
                    val now = System.currentTimeMillis()
                    if (now - lastClickTime > 500) {
                        lastClickTime = now
                        showAddSheet = true
                    }
                },
                expanded = !isScrolled,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { innerPadding ->
        if (workouts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No workouts yet.\nTap + to create one!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
                viewModel.moveWorkout(from.index, to.index)
            }
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(workouts.size, key = { workouts[it].id }) { index ->
                    val workout = workouts[index]
                    ReorderableItem(reorderableLazyListState, key = workout.id) { isDragging ->
                        LaunchedEffect(isDragging) {
                            if (!isDragging) viewModel.commitReorder()
                        }
                        WorkoutCard(
                            workout = workout,
                            onClick = { onWorkoutClick(workout.id) },
                            onEdit = { workoutToEdit = workout },
                            onDuplicate = { viewModel.duplicateWorkout(workout) },
                            onDelete = { viewModel.deleteWorkout(workout) },
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

    if (showAddSheet) {
        WorkoutEditSheet(
            workout = null,
            onDismiss = { showAddSheet = false },
            onConfirm = { name, description ->
                viewModel.addWorkout(name, description)
                showAddSheet = false
            }
        )
    }

    workoutToEdit?.let { workout ->
        WorkoutEditSheet(
            workout = workout,
            onDismiss = { workoutToEdit = null },
            onConfirm = { name, description ->
                viewModel.updateWorkout(workout.copy(name = name, description = description))
                workoutToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WorkoutCard(
    workout: Workout,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    dragModifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

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
                // Full-height drag handle strip — outside clickable area
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
                // Clickable content area
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onClick() }
                        .padding(top = 16.dp, bottom = 16.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (workout.description.isNotBlank()) {
                        Text(
                            text = workout.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
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
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditSheet(
    workout: Workout?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(workout?.name ?: "") }
    var description by remember { mutableStateOf(workout?.description ?: "") }
    val isEditing = workout != null
    val focusRequester = remember { FocusRequester() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(isEditing) {
        if (!isEditing) {
            delay(300)
            focusRequester.requestFocus()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isEditing) "Edit Workout" else "New Workout",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Workout name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                TextButton(
                    onClick = { onConfirm(name.trim(), description.trim()) },
                    enabled = name.isNotBlank()
                ) {
                    Text(if (isEditing) "Save" else "Create")
                }
            }
        }
    }
}
