package com.pax.habit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC, id DESC")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM completions")
    fun observeCompletions(): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM pauses")
    fun observePauses(): Flow<List<PauseEntity>>

    @Query("SELECT * FROM habits")
    suspend fun allHabits(): List<HabitEntity>

    @Query("SELECT * FROM completions")
    suspend fun allCompletions(): List<CompletionEntity>

    @Query("SELECT * FROM pauses")
    suspend fun allPauses(): List<PauseEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabit(id: Long): HabitEntity?

    @Query("SELECT * FROM pauses WHERE habitId = :id")
    suspend fun pausesFor(id: Long): List<PauseEntity>

    @Query("SELECT epochDay FROM completions WHERE habitId = :id")
    suspend fun completionDays(id: Long): List<Long>

    @Query("SELECT COALESCE(SUM(xp), 0) FROM completions WHERE habitId = :id")
    suspend fun sumXp(id: Long): Int

    @Query("SELECT * FROM pauses WHERE habitId = :id AND endDay IS NULL LIMIT 1")
    suspend fun openPause(id: Long): PauseEntity?

    @Insert
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    /** Returns -1 when the habit/date pair already exists (duplicate completion). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: CompletionEntity): Long

    @Query("DELETE FROM completions WHERE habitId = :habitId AND epochDay = :day")
    suspend fun deleteCompletion(habitId: Long, day: Long): Int

    @Insert
    suspend fun insertPause(pause: PauseEntity): Long

    @Update
    suspend fun updatePause(pause: PauseEntity)

    @Query("DELETE FROM pauses WHERE id = :id")
    suspend fun deletePause(id: Long)

    @Query("DELETE FROM habits")
    suspend fun clearHabits()

    @Query("DELETE FROM completions")
    suspend fun clearCompletions()

    @Query("DELETE FROM pauses")
    suspend fun clearPauses()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(list: List<HabitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(list: List<CompletionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPauses(list: List<PauseEntity>)
}
