package com.example.repattack.wear

import android.app.Application
import com.example.repattack.wear.data.WearWorkoutRepository

class WearRepAttackApp : Application() {
    val repository by lazy { WearWorkoutRepository(this) }
}
