package com.example.repattack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.repattack.data.dao.ExerciseDao
import com.example.repattack.data.dao.ExerciseLogDao
import com.example.repattack.data.dao.WorkoutDao
import com.example.repattack.data.model.Exercise
import com.example.repattack.data.model.ExerciseLog
import com.example.repattack.data.model.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                .addCallback(SeedCallback)
                .build().also { INSTANCE = it }
            }
        }

        /** Temporary seed data for development. Remove before release. */
        private object SeedCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = INSTANCE ?: return@launch
                    val workoutDao = database.workoutDao()
                    val exerciseDao = database.exerciseDao()

                    // Upper body workout
                    val upperId = workoutDao.insert(Workout(name = "Upper", description = "Push + Pull"))
                    exerciseDao.insert(Exercise(workoutId = upperId, name = "Bench Press", targetSets = 3, minReps = 6, maxReps = 8, restTime = "2-3 min", notes = "v shape - don't flare out", url = "https://youtu.be/pCGVSBk0blQ", orderIndex = 0))
                    exerciseDao.insert(Exercise(workoutId = upperId, name = "Rows", targetSets = 3, minReps = 6, maxReps = 8, restTime = "2-3 min", notes = "slow and steady", url = "https://youtu.be/FTCmwlfZ29A", orderIndex = 1))
                    exerciseDao.insert(Exercise(workoutId = upperId, name = "Incline Dumbbell Press", targetSets = 3, minReps = 8, maxReps = 10, restTime = "1-2 min", orderIndex = 2))
                    exerciseDao.insert(Exercise(workoutId = upperId, name = "Lateral Raises", targetSets = 3, minReps = 12, maxReps = 15, restTime = "1 min", orderIndex = 3))

                    // Lower body workout
                    val lowerId = workoutDao.insert(Workout(name = "Lower", description = "Legs + Core"))
                    exerciseDao.insert(Exercise(workoutId = lowerId, name = "Squats", targetSets = 4, minReps = 6, maxReps = 8, restTime = "2-3 min", orderIndex = 0))
                    exerciseDao.insert(Exercise(workoutId = lowerId, name = "Romanian Deadlifts", targetSets = 3, minReps = 8, maxReps = 10, restTime = "2 min", orderIndex = 1))
                    exerciseDao.insert(Exercise(workoutId = lowerId, name = "Leg Press", targetSets = 3, minReps = 10, maxReps = 12, restTime = "1-2 min", orderIndex = 2))
                }
            }
        }
    }
}
