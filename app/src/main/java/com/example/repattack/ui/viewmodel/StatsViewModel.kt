package com.example.repattack.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repattack.data.model.ExerciseCatalog
import com.example.repattack.data.model.ExerciseLog
import com.example.repattack.data.repository.RepAttackRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A single session's summary for one exercise.
 */
data class SessionSummary(
    val date: Long,
    val sets: List<ExerciseLog>,
    val maxWeight: Double?,
    val totalVolume: Double, // sum of (weight * reps) across all sets
    val topSetDisplay: String // e.g. "135 × 8"
)

/**
 * A data point for the progression chart.
 */
data class ChartDataPoint(
    val date: Long,
    val value: Float,
    val label: String // formatted date
)

class StatsViewModel(
    private val repository: RepAttackRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    val allExercises: StateFlow<List<ExerciseCatalog>> = repository.getAllUsedExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedExerciseId = MutableStateFlow<Long?>(null)
    val selectedExerciseId: StateFlow<Long?> = _selectedExerciseId.asStateFlow()

    init {
        // Restore last selected exercise
        val savedId = prefs.getLong("last_stats_exercise_id", -1L)
        if (savedId != -1L) _selectedExerciseId.value = savedId

        // Auto-select first exercise if no saved selection (or saved one no longer exists)
        viewModelScope.launch {
            allExercises.collect { exercises ->
                if (exercises.isNotEmpty()) {
                    val currentId = _selectedExerciseId.value
                    if (currentId == null || exercises.none { it.id == currentId }) {
                        _selectedExerciseId.value = exercises.first().id
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val logsForExercise: StateFlow<List<ExerciseLog>> = _selectedExerciseId
        .flatMapLatest { id ->
            if (id != null) repository.getLogsForExercise(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Sessions grouped by calendar day, newest first. */
    val sessions: StateFlow<List<SessionSummary>> = logsForExercise
        .map { logs ->
            logs.groupBy { it.date / 86_400_000L } // group by day
                .map { (_, sets) ->
                    val date = sets.first().date // use first log's timestamp for display
                    val sortedSets = sets.sortedBy { it.setNumber }
                    val maxWeight = sortedSets.mapNotNull { it.weight }.maxOrNull()
                    val totalVolume = sortedSets.sumOf { ((it.weight ?: 0.0).let { w -> if (w == 0.0) 1.0 else w }) * (it.reps ?: 0) }
                    val topSet = sortedSets.maxByOrNull { it.weight ?: 0.0 }
                    val topSetDisplay = if (topSet != null && topSet.weight != null) {
                        "${formatWeight(topSet.weight)} × ${topSet.reps ?: 0} reps"
                    } else ""
                    SessionSummary(date, sortedSets, maxWeight, totalVolume, topSetDisplay)
                }
                .sortedByDescending { it.date }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Chart data points — max weight per session over time. */
    val weightChartData: StateFlow<List<ChartDataPoint>> = sessions
        .map { sessionList ->
            val dateFormat = SimpleDateFormat("d/M", Locale.getDefault())
            sessionList
                .filter { it.maxWeight != null }
                .sortedBy { it.date }
                .map { session ->
                    ChartDataPoint(
                        date = session.date,
                        value = session.maxWeight!!.toFloat(),
                        label = dateFormat.format(Date(session.date))
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Chart data points — total volume per session over time. */
    val volumeChartData: StateFlow<List<ChartDataPoint>> = sessions
        .map { sessionList ->
            val dateFormat = SimpleDateFormat("d/M", Locale.getDefault())
            sessionList
                .sortedBy { it.date }
                .map { session ->
                    ChartDataPoint(
                        date = session.date,
                        value = session.totalVolume.toFloat(),
                        label = dateFormat.format(Date(session.date))
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectExercise(exerciseId: Long) {
        _selectedExerciseId.value = exerciseId
        prefs.edit().putLong("last_stats_exercise_id", exerciseId).apply()
    }

    fun updateSessionDate(oldDate: Long, newDate: Long) {
        viewModelScope.launch {
            repository.updateSessionDate(oldDate, newDate)
        }
    }

    private fun formatWeight(weight: Double?): String {
        if (weight == null) return "0"
        return if (weight == weight.toLong().toDouble()) weight.toLong().toString()
        else "%.1f".format(weight)
    }
}
