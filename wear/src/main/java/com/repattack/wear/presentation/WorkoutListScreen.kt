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
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.repattack.shared.SyncWorkout

@Composable
fun WorkoutListScreen(
    workouts: List<SyncWorkout>,
    onWorkoutClick: (Long) -> Unit
) {
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = contentPadding
        ) {
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec)
                ) {
                    Text(text = "Rep Attack")
                }
            }

            if (workouts.isEmpty()) {
                item {
                    Text(
                        text = "Open Rep Attack\non your phone\nto sync workouts",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                    )
                }
            } else {
                items(workouts, key = { it.id }) { workout ->
                    Button(
                        onClick = { onWorkoutClick(workout.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        label = {
                            Text(
                                text = workout.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        secondaryLabel = if (workout.description.isNotBlank()) {
                            {
                                Text(
                                    text = workout.description,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else null
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
