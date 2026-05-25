package com.repattack.wear.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.repattack.shared.SyncWorkout

@Composable
fun WorkoutDetailScreen(workout: SyncWorkout) {
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = contentPadding,
            flingBehavior = TransformingLazyColumnDefaults.snapFlingBehavior(columnState)
        ) {
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec)
                ) {
                    Text(text = workout.name)
                }
            }

            if (workout.exercises.isEmpty()) {
                item {
                    Text(
                        text = "No exercises",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                    )
                }
            } else {
                items(workout.exercises, key = { it.id }) { exercise ->
                    val target = buildString {
                        exercise.targetSets?.let { append("${it} × ") }
                        when {
                            exercise.minReps != null && exercise.maxReps != null &&
                                    exercise.minReps != exercise.maxReps ->
                                append("${exercise.minReps}-${exercise.maxReps}")

                            exercise.minReps != null -> append("${exercise.minReps}")
                            exercise.maxReps != null -> append("${exercise.maxReps}")
                        }
                    }

                    val restLine = if (exercise.restTime.isNotBlank()) {
                        "${exercise.restTime} rest"
                    } else null

                    val lastSession = if (exercise.lastSets.isNotEmpty()) {
                        val weights = exercise.lastSets.mapNotNull { it.weight }.distinct()
                        if (weights.size == 1 && weights[0] != null) {
                            val w = weights[0]
                            val weightStr =
                                if (w % 1.0 == 0.0) w.toInt().toString() else w.toString()
                            val repsStr = exercise.lastSets.joinToString("/") { set ->
                                set.reps?.toString() ?: "–"
                            }
                            "$weightStr × $repsStr"
                        } else {
                            exercise.lastSets.joinToString(" / ") { set ->
                                buildString {
                                    set.weight?.let { w ->
                                        append(
                                            if (w % 1.0 == 0.0) w.toInt()
                                                .toString() else w.toString()
                                        )
                                        append("×")
                                    }
                                    set.reps?.let { append(it) }
                                }
                            }
                        }
                    } else null

                    Card(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec)
                    ) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (target.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "• $target",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (restLine != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "• $restLine",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (lastSession != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "• $lastSession",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (exercise.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "• ${exercise.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
