package com.example.repattack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.repattack.data.dao.ExerciseCatalogDao
import com.example.repattack.data.dao.ExerciseLogDao
import com.example.repattack.data.dao.ProgramDao
import com.example.repattack.data.dao.WorkoutDao
import com.example.repattack.data.dao.WorkoutExerciseDao
import com.example.repattack.data.model.ExerciseCatalog
import com.example.repattack.data.model.ExerciseLog
import com.example.repattack.data.model.Program
import com.example.repattack.data.model.Workout
import com.example.repattack.data.model.WorkoutExercise

@Database(
    entities = [Program::class, Workout::class, ExerciseCatalog::class, WorkoutExercise::class, ExerciseLog::class],
    version = 1
)
abstract class RepAttackDatabase : RoomDatabase() {
    abstract fun programDao(): ProgramDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseCatalogDao(): ExerciseCatalogDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
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
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}
