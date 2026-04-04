package com.example.repattack.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.repattack.data.model.Exercise
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseEditDialog(
    exercise: Exercise?,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        targetSets: Int?,
        minReps: Int?,
        maxReps: Int?,
        restTime: String,
        notes: String,
        url: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var targetSets by remember { mutableStateOf(exercise?.targetSets?.toString() ?: "") }
    var minReps by remember { mutableStateOf(exercise?.minReps?.toString() ?: "") }
    var maxReps by remember { mutableStateOf(exercise?.maxReps?.toString() ?: "") }
    var restTime by remember { mutableStateOf(exercise?.restTime ?: "") }
    var notes by remember { mutableStateOf(exercise?.notes ?: "") }
    var url by remember { mutableStateOf(exercise?.url ?: "") }

    val isEditing = exercise != null
    val focusRequester = remember { FocusRequester() }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isEditing) "Edit Exercise" else "Add Exercise",
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
                value = targetSets,
                onValueChange = { targetSets = it.filter { c -> c.isDigit() } },
                label = { Text("Sets") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = minReps,
                    onValueChange = { minReps = it.filter { c -> c.isDigit() } },
                    label = { Text("Min reps") },
                    placeholder = { Text("e.g. 6") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = maxReps,
                    onValueChange = { maxReps = it.filter { c -> c.isDigit() } },
                    label = { Text("Max reps") },
                    placeholder = { Text("e.g. 8") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = restTime,
                onValueChange = { restTime = it },
                label = { Text("Rest time") },
                placeholder = { Text("e.g. 2-3 min") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
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
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                TextButton(
                    onClick = {
                        val min = minReps.toIntOrNull()
                        val max = maxReps.toIntOrNull()
                        val (finalMin, finalMax) = if (min != null && max != null && max < min) {
                            max to min
                        } else {
                            min to max
                        }
                        onConfirm(
                            name.trim(),
                            targetSets.toIntOrNull(),
                            finalMin,
                            finalMax,
                            restTime.trim(),
                            notes.trim(),
                            url.trim()
                        )
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text(if (isEditing) "Save" else "Add")
                }
            }
        }
    }
}
