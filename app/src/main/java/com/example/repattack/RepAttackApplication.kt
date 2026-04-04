package com.example.repattack

import android.app.Application
import com.example.repattack.data.RepAttackDatabase
import com.example.repattack.data.repository.RepAttackRepository

class RepAttackApplication : Application() {
    val database: RepAttackDatabase by lazy { RepAttackDatabase.getDatabase(this) }
    val repository: RepAttackRepository by lazy {
        RepAttackRepository(
            database.workoutDao(),
            database.exerciseDao(),
            database.exerciseLogDao()
        )
    }
}
