package com.repattack.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.repattack.wear.WearRepAttackApp
import com.repattack.wear.presentation.theme.WearRepAttackTheme
import kotlinx.coroutines.launch

class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as WearRepAttackApp).repository

        lifecycleScope.launch { repository.loadInitial() }

        setContent {
            WearRepAttackTheme {
                WearNavHost(repository = repository)
            }
        }
    }
}
