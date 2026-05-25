package com.repattack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.repattack.data.dao.ExerciseCatalogDao
import com.repattack.data.dao.ExerciseLogDao
import com.repattack.data.dao.ProgramDao
import com.repattack.data.dao.WorkoutDao
import com.repattack.data.dao.WorkoutExerciseDao
import com.repattack.data.model.ExerciseCatalog
import com.repattack.data.model.ExerciseLog
import com.repattack.data.model.Program
import com.repattack.data.model.Workout
import com.repattack.data.model.WorkoutExercise

@Database(
    entities = [Program::class, Workout::class, ExerciseCatalog::class, WorkoutExercise::class, ExerciseLog::class],
    version = 1,
    exportSchema = false
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
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
