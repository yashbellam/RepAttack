package com.example.repattack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.repattack.ui.screens.WorkoutDetailScreen
import com.example.repattack.ui.theme.RepAttackTheme

class WorkoutDetailActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_WORKOUT_ID = "workout_id"

        fun newIntent(context: Context, workoutId: Long): Intent {
            return Intent(context, WorkoutDetailActivity::class.java).apply {
                putExtra(EXTRA_WORKOUT_ID, workoutId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val workoutId = intent.getLongExtra(EXTRA_WORKOUT_ID, -1L)
        if (workoutId == -1L) {
            finish()
            return
        }

        setContent {
            RepAttackTheme {
                WorkoutDetailScreen(
                    workoutId = workoutId,
                    onBack = { finish() }
                )
            }
        }
    }
}
