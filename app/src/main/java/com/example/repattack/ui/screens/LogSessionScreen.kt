package com.example.repattack.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repattack.ui.AppViewModelFactory
import com.example.repattack.ui.viewmodel.ExerciseLogState
import com.example.repattack.ui.viewmodel.LogSessionViewModel
import com.example.repattack.ui.viewmodel.SetEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSessionScreen(
    workoutId: Long,
    onBack: () -> Unit,
    viewModel: LogSessionViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val workout by viewModel.workout.collectAsState()
    val logState by viewModel.logState.collectAsState()

    LaunchedEffect(workoutId) {
        viewModel.loadWorkout(workoutId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workout?.name ?: "Log Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                    onAddSet = { viewModel.addSet(exerciseIndex) },
                    onRemoveSet = { viewModel.removeSet(exerciseIndex) }
                )
            }
        }
    }
}

@Composable
private fun ExerciseLogCard(
    state: ExerciseLogState,
    onWeightDelta: (setIndex: Int, delta: Double) -> Unit,
    onSetWeight: (setIndex: Int, weight: Double) -> Unit,
    onRepsDelta: (setIndex: Int, delta: Int) -> Unit,
    onSetReps: (setIndex: Int, reps: Int) -> Unit,
    onToggleCompleted: (setIndex: Int) -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: () -> Unit
) {
    val exercise = state.exercise
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
                if (exercise.url.isNotBlank()) {
                    IconButton(
                        onClick = {
                            val url = if (exercise.url.startsWith("http")) exercise.url
                                else "https://${exercise.url}"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open link",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            val target = buildList {
                exercise.targetSets?.let { add("$it sets") }
                val min = exercise.minReps
                val max = exercise.maxReps
                if (min != null && max != null && min != max) add("$min-$max reps")
                else if (min != null) add("$min reps")
                else if (max != null) add("$max reps")
                if (exercise.restTime.isNotBlank()) add("${exercise.restTime} rest")
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SET", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                Text("WEIGHT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("REPS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(40.dp))
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
                TextButton(onClick = onRemoveSet) { Text("Remove set") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onAddSet) { Text("Add set") }
            }
        }
    }
}

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
    val textStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = MaterialTheme.typography.bodyLarge.fontSize
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$setNumber",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center
        )

        // Weight: [–] editable [+]
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
            FilledTonalIconButton(onClick = { onWeightDelta(-2.5) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease weight", modifier = Modifier.size(16.dp))
            }
            BasicTextField(
                value = formatWeight(set.weight),
                onValueChange = { text ->
                    val parsed = text.replace(",", ".").toDoubleOrNull()
                    if (parsed != null) onSetWeight(parsed)
                    else if (text.isEmpty()) onSetWeight(0.0)
                },
                textStyle = textStyle,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.width(52.dp)
            )
            FilledTonalIconButton(onClick = { onWeightDelta(2.5) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Increase weight", modifier = Modifier.size(16.dp))
            }
        }

        // Reps: [–] editable [+]
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
            FilledTonalIconButton(onClick = { onRepsDelta(-1) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease reps", modifier = Modifier.size(16.dp))
            }
            BasicTextField(
                value = "${set.reps}",
                onValueChange = { text ->
                    val parsed = text.filter { it.isDigit() }.toIntOrNull()
                    if (parsed != null) onSetReps(parsed)
                    else if (text.isEmpty()) onSetReps(0)
                },
                textStyle = textStyle,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.width(36.dp)
            )
            FilledTonalIconButton(onClick = { onRepsDelta(1) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Increase reps", modifier = Modifier.size(16.dp))
            }
        }

        FilledIconButton(
            onClick = onToggleCompleted,
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            colors = if (set.completed) {
                IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
            } else {
                IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            }
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = if (set.completed) "Completed" else "Mark complete",
                modifier = Modifier.size(16.dp),
                tint = if (set.completed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatWeight(weight: Double): String {
    return if (weight == weight.toLong().toDouble()) weight.toLong().toString()
    else "%.1f".format(weight)
}
