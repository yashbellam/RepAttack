package com.example.repattack.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repattack.ui.AppViewModelFactory
import com.example.repattack.ui.viewmodel.ChartDataPoint
import com.example.repattack.ui.viewmodel.SessionSummary
import com.example.repattack.ui.viewmodel.StatsViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import androidx.compose.runtime.LaunchedEffect
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val exercises by viewModel.allExercises.collectAsState()
    val selectedId by viewModel.selectedExerciseId.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val weightData by viewModel.weightChartData.collectAsState()
    val volumeData by viewModel.volumeChartData.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Stats") },
                subtitle = { Text("Track your progress") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                scrollBehavior = scrollBehavior,
                collapsedHeight = 64.dp
            )
        }
    ) { innerPadding ->
        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No exercises yet.\nLog a workout to see stats!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Exercise picker
                item {
                    ExerciseDropdown(
                        exercises = exercises.map { it.id to it.name },
                        selectedId = selectedId,
                        onSelect = { viewModel.selectExercise(it) }
                    )
                }

                item { Spacer(modifier = Modifier.height(4.dp)) }

                if (selectedId != null) {
                    // Chart
                    if (weightData.size >= 2 || volumeData.size >= 2) {
                        item {
                            Text(
                                text = "Progression",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        item {
                            ProgressionChart(
                                weightData = weightData,
                                volumeData = volumeData
                            )
                        }
                    } else if (weightData.isNotEmpty() || volumeData.isNotEmpty()) {
                        item {
                            Text(
                                text = "Log at least 2 sessions to see the chart",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Empty state
                    if (sessions.isEmpty()) {
                        item {
                            Text(
                                text = "No sessions logged yet.\nLog a workout to see stats!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                            )
                        }
                    }

                    // History header
                    if (sessions.isNotEmpty()) {
                        item {
                            Text(
                                text = "History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    // Session history cards
                    items(sessions, key = { it.date }) { session ->
                        SessionCard(
                            session = session,
                            onDateChangeExercise = { newDate ->
                                selectedId?.let { exId ->
                                    viewModel.updateExerciseSessionDate(exId, session.date, newDate)
                                }
                            },
                            onDateChangeSession = { newDate ->
                                viewModel.updateSessionDate(session.date, newDate)
                            },
                            onDeleteExercise = {
                                selectedId?.let { exId ->
                                    viewModel.deleteExerciseSession(exId, session.date)
                                }
                            },
                            onDeleteSession = {
                                viewModel.deleteSession(session.date)
                            },
                            exerciseName = exercises.find { it.id == selectedId }?.name ?: ""
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDropdown(
    exercises: List<Pair<Long, String>>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = exercises.find { it.first == selectedId }?.second ?: ""
    val focusManager = LocalFocusManager.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Exercise") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                focusManager.clearFocus()
            }
        ) {
            exercises.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProgressionChart(
    weightData: List<ChartDataPoint>,
    volumeData: List<ChartDataPoint>
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(weightData, volumeData) {
        if (weightData.isNotEmpty() || volumeData.isNotEmpty()) {
            modelProducer.runTransaction {
                if (volumeData.isNotEmpty()) {
                    lineSeries { series(volumeData.map { it.value.toDouble() }) }
                }
                if (weightData.isNotEmpty()) {
                    lineSeries { series(weightData.map { it.value.toDouble() }) }
                }
            }
        }
    }

    val weightColor = MaterialTheme.colorScheme.error
    val volumeColor = MaterialTheme.colorScheme.primary

    val weightLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(weightColor)),
        stroke = LineCartesianLayer.LineStroke.Continuous(),
        pointProvider = LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.Point(
                component = rememberShapeComponent(fill = Fill(weightColor), shape = CircleShape),
                size = 6.dp
            )
        ),
    )
    val volumeLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(volumeColor)),
        stroke = LineCartesianLayer.LineStroke.Continuous(),
        pointProvider = LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.Point(
                component = rememberShapeComponent(fill = Fill(volumeColor), shape = CircleShape),
                size = 6.dp
            )
        ),
    )

    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent(),
        indicator = { color -> ShapeComponent(fill = Fill(color), shape = CircleShape) },
        indicatorSize = 10.dp,
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(volumeLine),
                        verticalAxisPosition = Axis.Position.Vertical.Start,
                    ),
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(weightLine),
                        verticalAxisPosition = Axis.Position.Vertical.End,
                    ),
                    startAxis = VerticalAxis.rememberStart(),
                    endAxis = VerticalAxis.rememberEnd(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = { _, value, _ ->
                            val idx = value.toInt()
                            if (idx in volumeData.indices) volumeData[idx].label
                            else if (idx in weightData.indices) weightData[idx].label
                            else ""
                        }
                    ),
                    marker = marker,
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("● Volume", style = MaterialTheme.typography.labelSmall, color = volumeColor)
                Spacer(modifier = Modifier.width(16.dp))
                Text("● Max weight", style = MaterialTheme.typography.labelSmall, color = weightColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SessionCard(
    session: SessionSummary,
    onDateChangeExercise: (Long) -> Unit,
    onDateChangeSession: (Long) -> Unit,
    onDeleteExercise: () -> Unit,
    onDeleteSession: () -> Unit,
    exerciseName: String
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDateScopeDialog by remember { mutableStateOf(false) }
    var pendingNewDate by remember { mutableStateOf(0L) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val deleteButtonWidth = 72.dp
    val deleteButtonWidthPx = with(LocalDensity.current) { deleteButtonWidth.toPx() }
    val screenWidthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isDeleting by remember { mutableStateOf(false) }

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
                Column(modifier = Modifier.padding(16.dp)) {
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showDatePicker = true
                        },
                        shapes = ButtonDefaults.shapes(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dateFormat.format(Date(session.date)),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    session.sets.forEach { log ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Set ${log.setNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = buildString {
                                    log.weight?.let {
                                        append(if (it == it.toLong().toDouble()) it.toLong().toString() else "%.1f".format(it))
                                        append(" × ")
                                    }
                                    append("${log.reps ?: 0} reps")
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (session.totalVolume > 0) {
                            Text(
                                text = "Volume: ${session.totalVolume.toLong()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (session.maxWeight != null) {
                            Text(
                                text = "Max weight: ${session.topSetDisplay}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Date picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = session.date
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val utcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        utcCal.timeInMillis = millis
                        val localCal = java.util.Calendar.getInstance()
                        localCal.set(utcCal.get(java.util.Calendar.YEAR), utcCal.get(java.util.Calendar.MONTH), utcCal.get(java.util.Calendar.DAY_OF_MONTH), 12, 0, 0)
                        localCal.set(java.util.Calendar.MILLISECOND, 0)
                        pendingNewDate = localCal.timeInMillis
                        showDateScopeDialog = true
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

    // Date scope choice dialog
    if (showDateScopeDialog) {
        var moveScope by remember { mutableStateOf("exercise") }
        BasicAlertDialog(onDismissRequest = { showDateScopeDialog = false }) {
            Surface(
                shape = AlertDialogDefaults.shape,
                color = AlertDialogDefaults.containerColor,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column {
                    Text(
                        "Move which sets?",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp)
                    )
                    Text(
                        "Move just \"$exerciseName\" or all exercises logged on ${dateFormat.format(Date(session.date))}?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    )
                    HorizontalDivider()
                    Column {
                        ListItem(
                            modifier = Modifier.clickable { moveScope = "exercise" },
                            colors = ListItemDefaults.colors(containerColor = AlertDialogDefaults.containerColor),
                            headlineContent = { Text("This exercise") },
                            leadingContent = {
                                RadioButton(selected = moveScope == "exercise", onClick = { moveScope = "exercise" })
                            }
                        )
                        ListItem(
                            modifier = Modifier.clickable { moveScope = "session" },
                            colors = ListItemDefaults.colors(containerColor = AlertDialogDefaults.containerColor),
                            headlineContent = { Text("Whole session") },
                            leadingContent = {
                                RadioButton(selected = moveScope == "session", onClick = { moveScope = "session" })
                            }
                        )
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showDateScopeDialog = false },
                            shapes = ButtonDefaults.shapes()
                        ) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showDateScopeDialog = false
                                val vibrator = context.getSystemService(android.os.Vibrator::class.java)
                                if (moveScope == "session") {
                                    vibrator?.vibrate(android.os.VibrationEffect.createWaveform(
                                        longArrayOf(0, 50, 40, 150),
                                        intArrayOf(0, 200, 0, 200),
                                        -1
                                    ))
                                    onDateChangeSession(pendingNewDate)
                                } else {
                                    vibrator?.vibrate(android.os.VibrationEffect.createOneShot(150, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                    onDateChangeExercise(pendingNewDate)
                                }
                            },
                            shapes = ButtonDefaults.shapes()
                        ) { Text("Move") }
                    }
                }
            }
        }
    }

    // Delete scope choice dialog
    if (showDeleteDialog) {
        var deleteScope by remember { mutableStateOf("exercise") }
        BasicAlertDialog(onDismissRequest = {
            showDeleteDialog = false
            scope.launch { offsetX.animateTo(0f, tween(250, easing = FastOutSlowInEasing)) }
        }) {
            Surface(
                shape = AlertDialogDefaults.shape,
                color = AlertDialogDefaults.containerColor,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column {
                    Text(
                        "Delete which sets?",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp)
                    )
                    Text(
                        "Delete just \"$exerciseName\" or all exercises logged on ${dateFormat.format(Date(session.date))}?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    )
                    HorizontalDivider()
                    Column {
                        ListItem(
                            modifier = Modifier.clickable { deleteScope = "exercise" },
                            colors = ListItemDefaults.colors(containerColor = AlertDialogDefaults.containerColor),
                            headlineContent = { Text("This exercise") },
                            leadingContent = {
                                RadioButton(selected = deleteScope == "exercise", onClick = { deleteScope = "exercise" })
                            }
                        )
                        ListItem(
                            modifier = Modifier.clickable { deleteScope = "session" },
                            colors = ListItemDefaults.colors(containerColor = AlertDialogDefaults.containerColor),
                            headlineContent = { Text("Whole session") },
                            leadingContent = {
                                RadioButton(selected = deleteScope == "session", onClick = { deleteScope = "session" })
                            }
                        )
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                showDeleteDialog = false
                                scope.launch { offsetX.animateTo(0f, tween(250, easing = FastOutSlowInEasing)) }
                            },
                            shapes = ButtonDefaults.shapes()
                        ) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                scope.launch {
                                    isDeleting = true
                                    val vibrator = context.getSystemService(android.os.Vibrator::class.java)
                                    if (deleteScope == "session") {
                                        vibrator?.vibrate(android.os.VibrationEffect.createWaveform(
                                            longArrayOf(0, 50, 40, 300),
                                            intArrayOf(0, 200, 0, 200),
                                            -1
                                        ))
                                        offsetX.animateTo(-screenWidthPx, tween(250))
                                        onDeleteSession()
                                    } else {
                                        vibrator?.vibrate(android.os.VibrationEffect.createOneShot(300, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                        offsetX.animateTo(-screenWidthPx, tween(250))
                                        onDeleteExercise()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shapes = ButtonDefaults.shapes()
                        ) { Text("Delete") }
                    }
                }
            }
        }
    }
}

