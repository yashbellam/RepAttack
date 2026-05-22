package com.example.repattack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.repattack.ui.screens.ExerciseHistoryScreen
import com.example.repattack.ui.theme.RepAttackTheme

class ExerciseHistoryActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_EXERCISE_ID = "exercise_id"
        private const val EXTRA_EXERCISE_NAME = "exercise_name"

        fun newIntent(context: Context, exerciseId: Long, exerciseName: String): Intent {
            return Intent(context, ExerciseHistoryActivity::class.java).apply {
                putExtra(EXTRA_EXERCISE_ID, exerciseId)
                putExtra(EXTRA_EXERCISE_NAME, exerciseName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val exerciseId = intent.getLongExtra(EXTRA_EXERCISE_ID, -1L)
        if (exerciseId == -1L) {
            finish()
            return
        }
        val exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME).orEmpty()

        setContent {
            RepAttackTheme {
                ExerciseHistoryScreen(
                    exerciseId = exerciseId,
                    exerciseName = exerciseName,
                    onBack = { finish() }
                )
            }
        }
    }
}
