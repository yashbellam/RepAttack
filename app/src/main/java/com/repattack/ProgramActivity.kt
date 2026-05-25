package com.repattack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.repattack.ui.screens.ProgramScreen
import com.repattack.ui.theme.RepAttackTheme

class ProgramActivity : ComponentActivity() {

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ProgramActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RepAttackTheme {
                ProgramScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}
