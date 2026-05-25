package com.repattack.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.repattack.ui.AppViewModelFactory
import com.repattack.ui.viewmodel.ExerciseLogState
import com.repattack.ui.viewmodel.LogSessionViewModel
import com.repattack.ui.viewmodel.SetEntry
import com.repattack.data.model.WorkoutExercise
import com.repattack.data.model.WorkoutExerciseWithCatalog
import com.repattack.ui.theme.RepAttackTheme
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LogSessionScreen(
    workoutId: Long,
    onBack: () -> Unit,
    onEditExercises: (Long) -> Unit,
    onShowExerciseHistory: (exerciseId: Long, exerciseName: String) -> Unit,
    viewModel: LogSessionViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val workout by viewModel.workout.collectAsState()
    val logState by viewModel.logState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var sessionDateMillis by remember { mutableStateOf(0L) }

    LaunchedEffect(workoutId) {
        viewModel.loadWorkout(workoutId)
        sessionDateMillis = viewModel.getSessionDate()
    }

    // Refresh when returning from edit exercises screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(workout?.name ?: "Log Workout") },
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
                            showDatePicker = true
                        },
                        modifier = Modifier.size(wideSize),
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Change date")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEditExercises(workoutId)
                        },
                        modifier = Modifier.size(wideSize),
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit exercises")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        }
    ) { innerPadding ->
        val focusManager = LocalFocusManager.current
        if (logState.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No exercises yet.\nTap the edit button to add some!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(logState) { exerciseIndex, exerciseLogState ->
                ExerciseLogCard(
                    state = exerciseLogState,
                    onWeightDelta = { setIndex, delta ->
                        viewModel.updateWeight(exerciseIndex, setIndex, delta)
                    },
                    onSetWeight = { setIndex, weight ->
                        viewModel.setWeight(exerciseIndex, setIndex, weight)
                    },
                    onRepsDelta = { setIndex, delta ->
                        viewModel.updateReps(exerciseIndex, setIndex, delta)
                    },
                    onSetReps = { setIndex, reps ->
                        viewModel.setReps(exerciseIndex, setIndex, reps)
                    },
                    onToggleCompleted = { setIndex ->
                        viewModel.toggleSetCompleted(exerciseIndex, setIndex)
                    },
                    onToggleAll = { viewModel.toggleAllSets(exerciseIndex) },
                    onAddSet = { viewModel.addSet(exerciseIndex) },
                    onRemoveSet = { viewModel.removeSet(exerciseIndex) },
                    onShowHistory = {
                        val ex = exerciseLogState.exerciseWithCatalog
                        onShowExerciseHistory(ex.workoutExercise.exerciseId, ex.name)
                    }
                )
            }
        }
        }
    }

    if (showDatePicker) {
        // Convert local timestamp to UTC for DatePicker
        val initialUtc = remember(sessionDateMillis) {
            val localCal = Calendar.getInstance()
            localCal.timeInMillis = sessionDateMillis
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCal.set(localCal.get(Calendar.YEAR), localCal.get(Calendar.MONTH), localCal.get(
                Calendar.DAY_OF_MONTH), 0, 0, 0)
            utcCal.set(Calendar.MILLISECOND, 0)
            utcCal.timeInMillis
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialUtc
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // DatePicker returns UTC midnight — extract date parts in UTC,
                        // then build local time preserving current hour/minute
                        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        utcCal.timeInMillis = millis
                        val localCal = Calendar.getInstance()
                        localCal.set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH), utcCal.get(
                            Calendar.DAY_OF_MONTH))
                        sessionDateMillis = localCal.timeInMillis
                        viewModel.setSessionDate(localCal.timeInMillis)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExerciseLogCard(
    state: ExerciseLogState,
    onWeightDelta: (setIndex: Int, delta: Double) -> Unit,
    onSetWeight: (setIndex: Int, weight: Double) -> Unit,
    onRepsDelta: (setIndex: Int, delta: Int) -> Unit,
    onSetReps: (setIndex: Int, reps: Int) -> Unit,
    onToggleCompleted: (setIndex: Int) -> Unit,
    onToggleAll: () -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: () -> Unit,
    onShowHistory: () -> Unit
) {
    val exercise = state.exerciseWithCatalog
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                val hapticHistory = LocalHapticFeedback.current
                FilledTonalIconButton(
                    onClick = {
                        hapticHistory.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowHistory()
                    },
                    modifier = Modifier.size(36.dp),
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "View history",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                val hasLink = exercise.url.isNotBlank()
                val hapticLink = LocalHapticFeedback.current
                FilledTonalIconButton(
                    onClick = {
                        hapticLink.performHapticFeedback(HapticFeedbackType.LongPress)
                        val url = if (exercise.url.startsWith("http")) exercise.url
                            else "https://${exercise.url}"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    enabled = hasLink,
                    modifier = Modifier.size(36.dp),
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = if (hasLink) "Open link" else "No link",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            val target = buildList {
                exercise.workoutExercise.targetSets?.let { add("$it sets") }
                val min = exercise.workoutExercise.minReps
                val max = exercise.workoutExercise.maxReps
                if (min != null && max != null && min != max) add("$min-$max reps")
                else if (min != null) add("$min reps")
                else if (max != null) add("$max reps")
                if (exercise.workoutExercise.restTime.isNotBlank()) add("${exercise.workoutExercise.restTime} rest")
            }
            if (target.isNotEmpty()) {
                Text(
                    text = target.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

            Spacer(modifier = Modifier.height(12.dp))

            // Column headers — aligned to match SetRow layout
            val allCompleted = state.sets.all { it.completed }
            val haptic = LocalHapticFeedback.current

            // Stronger haptic when all sets become completed — skip initial composition
            var previousAllCompleted by remember { mutableStateOf(allCompleted) }
            LaunchedEffect(allCompleted) {
                if (allCompleted && !previousAllCompleted && state.sets.isNotEmpty()) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                previousAllCompleted = allCompleted
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SET", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("WEIGHT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("REPS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Check-all toggle button — M3E shape morph
                OutlinedIconToggleButton(
                    checked = allCompleted,
                    onCheckedChange = {
                        haptic.performHapticFeedback(
                            if (allCompleted) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn
                        )
                        // LaunchedEffect adds a bonus LongPress when allCompleted becomes true
                        onToggleAll()
                    },
                    modifier = Modifier.size(32.dp),
                    shapes = IconButtonDefaults.toggleableShapes(),
                    colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primary,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = if (allCompleted) "Uncheck all" else "Check all",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            state.sets.forEachIndexed { setIndex, set ->
                if (setIndex > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
                SetRow(
                    setNumber = setIndex + 1,
                    set = set,
                    onWeightDelta = { delta -> onWeightDelta(setIndex, delta) },
                    onSetWeight = { weight -> onSetWeight(setIndex, weight) },
                    onRepsDelta = { delta -> onRepsDelta(setIndex, delta) },
                    onSetReps = { reps -> onSetReps(setIndex, reps) },
                    onToggleCompleted = { onToggleCompleted(setIndex) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(shapes = ButtonDefaults.shapes(), onClick = onRemoveSet) { Text("Remove set") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(shapes = ButtonDefaults.shapes(), onClick = onAddSet) { Text("Add set") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SetRow(
    setNumber: Int,
    set: SetEntry,
    onWeightDelta: (Double) -> Unit,
    onSetWeight: (Double) -> Unit,
    onRepsDelta: (Int) -> Unit,
    onSetReps: (Int) -> Unit,
    onToggleCompleted: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val textStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = MaterialTheme.typography.bodyLarge.fontSize
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Set number badge
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$setNumber",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Weight: [–] editable [+]
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
            FilledTonalIconButton(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick); onWeightDelta(-2.5) },
                modifier = Modifier.size(32.dp),
                shapes = IconButtonDefaults.shapes(),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF3D2228),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease weight", modifier = Modifier.size(16.dp))
            }
            var weightText by remember(set.weight) {
                mutableStateOf(formatWeight(set.weight))
            }
            BasicTextField(
                value = weightText,
                onValueChange = { text ->
                    if (text.isEmpty() || text.replace(",", ".").toDoubleOrNull() != null || text == "." || text == ",") {
                        weightText = text
                        val parsed = text.replace(",", ".").toDoubleOrNull()
                        if (parsed != null) onSetWeight(parsed)
                    }
                },
                textStyle = textStyle,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.width(52.dp).onFocusChanged {
                    if (!it.isFocused) weightText = formatWeight(set.weight)
                }
            )
            FilledTonalIconButton(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick); onWeightDelta(2.5) },
                modifier = Modifier.size(32.dp),
                shapes = IconButtonDefaults.shapes(),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF223D28),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase weight", modifier = Modifier.size(16.dp))
            }
        }

        // Reps: [–] editable [+]
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
            FilledTonalIconButton(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick); onRepsDelta(-1) },
                modifier = Modifier.size(32.dp),
                shapes = IconButtonDefaults.shapes(),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF3D2228),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease reps", modifier = Modifier.size(16.dp))
            }
            var repsText by remember(set.reps) {
                mutableStateOf("${set.reps}")
            }
            BasicTextField(
                value = repsText,
                onValueChange = { text ->
                    if (text.isEmpty() || text.all { it.isDigit() }) {
                        repsText = text
                        val parsed = text.toIntOrNull()
                        if (parsed != null) onSetReps(parsed)
                    }
                },
                textStyle = textStyle,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.width(36.dp).onFocusChanged {
                    if (!it.isFocused) repsText = "${set.reps}"
                }
            )
            FilledTonalIconButton(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick); onRepsDelta(1) },
                modifier = Modifier.size(32.dp),
                shapes = IconButtonDefaults.shapes(),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF223D28),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase reps", modifier = Modifier.size(16.dp))
            }
        }

        OutlinedIconToggleButton(
            checked = set.completed,
            onCheckedChange = {
                haptic.performHapticFeedback(
                    if (set.completed) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn
                )
                onToggleCompleted()
            },
            modifier = Modifier.size(32.dp),
            shapes = IconButtonDefaults.toggleableShapes(),
            colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                checkedContainerColor = MaterialTheme.colorScheme.primary,
                checkedContentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = if (set.completed) "Completed" else "Mark complete",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun formatWeight(weight: Double): String {
    return if (weight == weight.toLong().toDouble()) weight.toLong().toString()
    else "%.1f".format(weight)
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewExerciseLogCard() {
    RepAttackTheme {
        ExerciseLogCard(
            state = ExerciseLogState(
                exerciseWithCatalog = WorkoutExerciseWithCatalog(
                    workoutExercise = WorkoutExercise(
                        id = 1, workoutId = 1, exerciseId = 1,
                        targetSets = 3, minReps = 6, maxReps = 8,
                        restTime = "2-3 min"
                    ),
                    name = "Bench Press",
                    url = "https://youtu.be/example",
                    notes = "v shape - don't flare out"
                ),
                sets = listOf(
                    SetEntry(weight = 60.0, reps = 8, completed = true),
                    SetEntry(weight = 60.0, reps = 7, completed = true),
                    SetEntry(weight = 60.0, reps = 6, completed = false),
                )
            ),
            onWeightDelta = { _, _ -> },
            onSetWeight = { _, _ -> },
            onRepsDelta = { _, _ -> },
            onSetReps = { _, _ -> },
            onToggleCompleted = { },
            onToggleAll = { },
            onAddSet = { },
            onRemoveSet = { },
            onShowHistory = { }
        )
    }
}

