package com.example.repattack.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repattack.data.model.ExerciseCatalog
import com.example.repattack.ui.AppViewModelFactory
import com.example.repattack.ui.viewmodel.ExerciseCatalogViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExerciseCatalogScreen(
    onBack: () -> Unit,
    viewModel: ExerciseCatalogViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val exercises by viewModel.exercises.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val filteredExercises = if (searchQuery.isBlank()) exercises
        else exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lazyListState = rememberLazyListState()
    val isScrolled = lazyListState.firstVisibleItemIndex > 0 ||
        lazyListState.firstVisibleItemScrollOffset > 0

    var exerciseToEdit by remember { mutableStateOf<ExerciseCatalog?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Exercise Catalog") },
                subtitle = { Text("${exercises.size} exercises") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
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
        },
        floatingActionButton = {
            SmallExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                expanded = !isScrolled,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { innerPadding ->
        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No exercises in catalog yet.\nTap + to add one!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search exercises") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                items(filteredExercises.size, key = { filteredExercises[it].id }) { index ->
                    val exercise = filteredExercises[index]
                    Box(modifier = Modifier.animateItem()) {
                        CatalogExerciseCard(
                            exercise = exercise,
                            onEdit = { exerciseToEdit = exercise },
                            onDuplicate = { viewModel.duplicateExercise(exercise) },
                            onDelete = { viewModel.deleteExercise(exercise) }
                        )
                    }
                }
            }
        }
    }

    exerciseToEdit?.let { exercise ->
        CatalogEditSheet(
            exercise = exercise,
            onDismiss = { exerciseToEdit = null },
            onConfirm = { name, notes, url ->
                viewModel.updateExercise(exercise.copy(name = name, notes = notes, url = url))
                exerciseToEdit = null
            }
        )
    }

    if (showAddSheet) {
        CatalogEditSheet(
            exercise = null,
            onDismiss = { showAddSheet = false },
            onConfirm = { name, notes, url ->
                viewModel.addExercise(name = name, notes = notes, url = url)
                showAddSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CatalogExerciseCard(
    exercise: ExerciseCatalog,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val deleteButtonWidth = 72.dp
    val deleteButtonWidthPx = with(LocalDensity.current) { deleteButtonWidth.toPx() }
    val screenWidthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    fun performDelete() {
        scope.launch {
            isDeleting = true
            val vibrator = context.getSystemService(android.os.Vibrator::class.java)
            vibrator?.vibrate(android.os.VibrationEffect.createOneShot(300, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            offsetX.animateTo(-screenWidthPx, tween(250))
            onDelete()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete exercise?") },
            text = { Text("Are you sure you want to delete \"${exercise.name}\"? This will remove it from all workouts and delete all logged history.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        performDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(shapes = ButtonDefaults.shapes(), onClick = {
                    showDeleteDialog = false
                    scope.launch { offsetX.animateTo(0f, tween(250, easing = FastOutSlowInEasing)) }
                }) { Text("Cancel") }
            }
        )
    }

    val swipeFraction = (-offsetX.value / deleteButtonWidthPx).coerceIn(0f, 1f)
    val revealColor = lerp(
        MaterialTheme.colorScheme.surfaceContainer,
        MaterialTheme.colorScheme.errorContainer,
        swipeFraction
    )

    if (!isDeleting || offsetX.value > -screenWidthPx) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(revealColor, shape = MaterialTheme.shapes.medium)
            )

            if (swipeFraction > 0.3f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(end = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    FilledIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showDeleteDialog = true
                        },
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .draggable(
                        state = rememberDraggableState { delta ->
                            scope.launch {
                                val newOffset = (offsetX.value + delta).coerceIn(-deleteButtonWidthPx, 0f)
                                offsetX.snapTo(newOffset)
                            }
                        },
                        orientation = Orientation.Horizontal,
                        onDragStopped = {
                            scope.launch {
                                val target = if (offsetX.value < -deleteButtonWidthPx / 2) -deleteButtonWidthPx else 0f
                                offsetX.animateTo(target, tween(250, easing = FastOutSlowInEasing))
                            }
                        }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (exercise.notes.isNotBlank()) {
                            Text(
                                text = exercise.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        if (exercise.url.isNotBlank()) {
                            Text(
                                text = exercise.url,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    val narrowSize = IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Narrow)
                    val buttonShapes = IconButtonDefaults.shapes()
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
                    Spacer(modifier = Modifier.width(8.dp))
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
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CatalogEditSheet(
    exercise: ExerciseCatalog?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, notes: String, url: String) -> Unit
) {
    val isEditing = exercise != null
    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var notes by remember { mutableStateOf(exercise?.notes ?: "") }
    var url by remember { mutableStateOf(exercise?.url ?: "") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!isEditing) {
            snapshotFlow { sheetState.isVisible }.first { it }
            focusRequester.requestFocus()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isEditing) "Edit Exercise" else "New Exercise",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Exercise name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Link") },
                placeholder = { Text("e.g. youtube.com/watch?v=...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } }
                ) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    shapes = ButtonDefaults.shapes(),
                    onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { onConfirm(name, notes, url) } },
                    enabled = name.isNotBlank()
                ) { Text(if (isEditing) "Save" else "Add") }
            }
        }
    }
}

