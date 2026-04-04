package com.example.repattack.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.repattack.data.model.Exercise

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Exercise" else "Add Exercise") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                    label = { Text("Reference URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        name,
                        targetSets.toIntOrNull(),
                        minReps.toIntOrNull(),
                        maxReps.toIntOrNull(),
                        restTime,
                        notes,
                        url
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (isEditing) "Save" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
