package com.example.repattack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.repattack.data.dao.ExerciseDao
import com.example.repattack.data.dao.ExerciseLogDao
import com.example.repattack.data.dao.WorkoutDao
import com.example.repattack.data.model.Exercise
import com.example.repattack.data.model.ExerciseLog
import com.example.repattack.data.model.Workout

@Database(
    entities = [Workout::class, Exercise::class, ExerciseLog::class],
    version = 1
)
abstract class RepAttackDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseLogDao(): ExerciseLogDao

    companion object {
        @Volatile
        private var INSTANCE: RepAttackDatabase? = null

        fun getDatabase(context: Context): RepAttackDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    RepAttackDatabase::class.java,
                    "repattack_database"
                )
                .build().also { INSTANCE = it }
            }
        }
    }
}
