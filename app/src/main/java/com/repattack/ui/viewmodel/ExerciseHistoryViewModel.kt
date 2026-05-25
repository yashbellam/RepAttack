package com.repattack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import com.repattack.data.model.ExerciseLog
import com.repattack.data.repository.RepAttackRepository
import com.repattack.ui.repAttackApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Single-exercise variant of [StatsViewModel] — no dropdown / persistence.
 * Surfaces the same [SessionSummary] / [ChartDataPoint] types so the
 * [com.repattack.ui.screens.ExerciseHistoryScreen] can reuse
 * `ProgressionChart` and `SessionCard` from `StatsScreen`.
 */
class ExerciseHistoryViewModel(
    private val repository: RepAttackRepository,
    private val exerciseId: Long
) : ViewModel() {

    private val logs: StateFlow<List<ExerciseLog>> =
        repository.getLogsForExercise(exerciseId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Sessions grouped by calendar day, newest first. */
    val sessions: StateFlow<List<SessionSummary>> = logs
        .map { exerciseLogs ->
            exerciseLogs.groupBy { it.date / 86_400_000L }
                .map { (_, sets) ->
                    val date = sets.first().date
                    val sortedSets = sets.sortedBy { it.setNumber }
                    val maxWeight = sortedSets.mapNotNull { it.weight }.maxOrNull()
                    val totalVolume = sortedSets.sumOf {
                        ((it.weight ?: 0.0).let { w -> if (w == 0.0) 1.0 else w }) * (it.reps ?: 0)
                    }
                    val topSet = sortedSets.maxByOrNull { it.weight ?: 0.0 }
                    val topSetDisplay = if (topSet != null && topSet.weight != null) {
                        "${formatWeight(topSet.weight)} × ${topSet.reps ?: 0} reps"
                    } else ""
                    SessionSummary(date, sortedSets, maxWeight, totalVolume, topSetDisplay)
                }
                .sortedByDescending { it.date }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    fun updateSessionDate(oldDate: Long, newDate: Long) {
        viewModelScope.launch {
            repository.updateSessionDate(oldDate, newDate)
        }
    }

    fun updateExerciseSessionDate(oldDate: Long, newDate: Long) {
        viewModelScope.launch {
            repository.updateExerciseSessionDate(exerciseId, oldDate, newDate)
        }
    }

    fun deleteSession(date: Long) {
        viewModelScope.launch {
            repository.deleteSessionByDate(date)
        }
    }

    fun deleteExerciseSession(date: Long) {
        viewModelScope.launch {
            repository.deleteExerciseSessionByDate(exerciseId, date)
        }
    }

    private fun formatWeight(weight: Double?): String {
        if (weight == null) return "0"
        return if (weight == weight.toLong().toDouble()) weight.toLong().toString()
        else "%.1f".format(weight)
    }

    companion object {
        fun factory(exerciseId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ExerciseHistoryViewModel(repAttackApplication().repository, exerciseId)
            }
        }
    }
}
