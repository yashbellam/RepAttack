package com.example.repattack.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repattack.data.model.Program
import com.example.repattack.ui.AppViewModelFactory
import com.example.repattack.ui.viewmodel.ProgramViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProgramScreen(
    onBack: () -> Unit,
    viewModel: ProgramViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val programs by viewModel.programs.collectAsState()
    val allWorkouts by viewModel.allWorkouts.collectAsState()
    val activeProgramId by viewModel.activeProgramId.collectAsState()
    val workoutCountByProgram = allWorkouts.groupBy { it.programId }.mapValues { it.value.size }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lazyListState = rememberLazyListState()
    val isScrolled = lazyListState.firstVisibleItemIndex > 0 ||
        lazyListState.firstVisibleItemScrollOffset > 0

    var programToEdit by remember { mutableStateOf<Program?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredPrograms = if (searchQuery.isBlank()) programs
        else programs.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Programs") },
                subtitle = { Text("${programs.size} programs") },
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
        if (programs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No programs yet.\nTap + to add one!",
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
                        label = { Text("Search programs") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                items(filteredPrograms.size, key = { filteredPrograms[it].id }) { index ->
                    val program = filteredPrograms[index]
                    Box(modifier = Modifier.animateItem()) {
                        ProgramCard(
                            program = program,
                            isActive = program.id == activeProgramId,
                            workoutCount = workoutCountByProgram[program.id] ?: 0,
                            onSetActive = { viewModel.setActiveProgram(program.id) },
                            onEdit = { programToEdit = program },
                            onDuplicate = { viewModel.duplicateProgram(program) },
                            onDelete = { viewModel.deleteProgram(program) }
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "= active program. Tap a program to set it as active.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    programToEdit?.let { program ->
        ProgramEditSheet(
            name = program.name,
            notes = program.notes,
            url = program.url,
            title = "Edit Program",
            onDismiss = { programToEdit = null },
            onConfirm = { newName, newNotes, newUrl ->
                viewModel.updateProgram(program.copy(name = newName, notes = newNotes, url = newUrl))
                programToEdit = null
            }
        )
    }

    if (showAddSheet) {
        ProgramEditSheet(
            name = "",
            notes = "",
            url = "",
            title = "New Program",
            onDismiss = { showAddSheet = false },
            onConfirm = { name, notes, url ->
                viewModel.addProgram(name, notes, url)
                showAddSheet = false
            },
            autofocus = true
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProgramCard(
    program: Program,
    isActive: Boolean,
    workoutCount: Int,
    onSetActive: () -> Unit,
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
    val swipeSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val deleteSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    fun performDelete() {
        scope.launch {
            isDeleting = true
            val vibrator = context.getSystemService(android.os.Vibrator::class.java)
            vibrator?.vibrate(android.os.VibrationEffect.createOneShot(300, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            offsetX.animateTo(-screenWidthPx, deleteSpec)
            onDelete()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete program?") },
            text = { Text("Are you sure you want to delete \"${program.name}\"? All workouts in this program will also be deleted.") },
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
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        showDeleteDialog = false
                        scope.launch { offsetX.animateTo(0f, swipeSpec) }
                    }
                ) { Text("Cancel") }
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
                                offsetX.animateTo(target, swipeSpec)
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
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSetActive()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isActive) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Active",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = program.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (program.notes.isNotBlank()) "${workoutCount} workouts · ${program.notes}"
                                else "$workoutCount workouts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
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
private fun ProgramEditSheet(
    name: String,
    notes: String,
    url: String,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    autofocus: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var editName by remember { mutableStateOf(name) }
    var editNotes by remember { mutableStateOf(notes) }
    var editUrl by remember { mutableStateOf(url) }
    val focusRequester = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Program name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp)
                .focusRequester(focusRequester)
        )
        OutlinedTextField(
            value = editNotes,
            onValueChange = { editNotes = it },
            label = { Text("Notes") },
            singleLine = false,
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp)
        )
        OutlinedTextField(
            value = editUrl,
            onValueChange = { editUrl = it },
            label = { Text("Link") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } },
                shapes = ButtonDefaults.shapes()
            ) { Text("Cancel") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (editName.isNotBlank()) {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onConfirm(editName.trim(), editNotes.trim(), editUrl.trim()) }
                    }
                },
                enabled = editName.isNotBlank(),
                shapes = ButtonDefaults.shapes()
            ) { Text("Save") }
        }
    }

    if (autofocus) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}
