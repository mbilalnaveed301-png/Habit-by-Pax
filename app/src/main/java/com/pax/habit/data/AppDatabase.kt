package com.pax.habit.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HabitEntity::class, CompletionEntity::class, PauseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): HabitDao
}
