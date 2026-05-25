package com.repattack.wear

import android.app.Application
import com.repattack.wear.data.WearWorkoutRepository

class WearRepAttackApp : Application() {
    val repository by lazy { WearWorkoutRepository(this) }
}
