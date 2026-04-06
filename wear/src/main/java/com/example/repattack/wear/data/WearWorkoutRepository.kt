package com.example.repattack.wear.data

import android.content.Context
import com.example.repattack.shared.SyncPaths
import com.example.repattack.shared.SyncWorkout
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json

/**
 * Watch-side repository that reads workout data from the Wearable DataLayer.
 * Listens for changes in-process — no background service needed.
 */
class WearWorkoutRepository(context: Context) : DataClient.OnDataChangedListener {
    private val dataClient: DataClient = Wearable.getDataClient(context)

    private val _workouts = MutableStateFlow<List<SyncWorkout>>(emptyList())
    val workouts: StateFlow<List<SyncWorkout>> = _workouts.asStateFlow()

    init {
        dataClient.addListener(this)
    }

    override fun onDataChanged(dataEvents: com.google.android.gms.wearable.DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == SyncPaths.WORKOUTS
            ) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val json = dataMap.getString(SyncPaths.KEY_WORKOUTS_JSON) ?: continue
                _workouts.value = Json.decodeFromString<List<SyncWorkout>>(json)
            }
        }
    }

    /** Load initial data from the DataLayer on startup. */
    suspend fun loadInitial() {
        try {
            val dataItems = dataClient.getDataItems().await()
            for (item in dataItems) {
                if (item.uri.path == SyncPaths.WORKOUTS) {
                    val dataMap = DataMapItem.fromDataItem(item).dataMap
                    val json = dataMap.getString(SyncPaths.KEY_WORKOUTS_JSON) ?: continue
                    _workouts.value = Json.decodeFromString<List<SyncWorkout>>(json)
                }
            }
            dataItems.release()
        } catch (_: Exception) {
            // No data yet — phone hasn't synced
        }
    }
}
