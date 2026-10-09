package com.pax.habit.data

import android.content.Context
import androidx.room.withTransaction
import com.pax.habit.backup.BackupCodec
import com.pax.habit.backup.BackupData
import com.pax.habit.domain.AppState
import com.pax.habit.domain.buildState
import com.pax.habit.notify.ReminderScheduler
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class HabitRepository(
    private val context: Context,
    val db: AppDatabase,
    val settingsStore: SettingsStore
) {
    private val dao = db.dao()

    val habits = dao.observeHabits()
    val completions = dao.observeCompletions()
    val pauses = dao.observePauses()
    val settings = settingsStore.settings

    suspend fun saveHabit(habit: HabitEntity): Long {
        val id = if (habit.id == 0L) {
            dao.insertHabit(habit)
        } else {
            dao.updateHabit(habit)
            habit.id
        }
        val saved = habit.copy(id = id)
        if (dao.openPause(id) == null) ReminderScheduler.schedule(context, saved) else ReminderScheduler.cancel(context, id)
        return id
    }

    suspend fun deleteHabit(id: Long) {
        val xp = dao.sumXp(id)
        if (xp > 0) settingsStore.addXpBanked(xp)
        ReminderScheduler.cancel(context, id)
        dao.deleteHabit(id)
    }

    /** Returns false when that habit/date was already completed (the DB enforces uniqueness). */
    suspend fun completeHabit(habitId: Long, day: Long, xp: Int): Boolean {
        val today = LocalDate.now().toEpochDay()
        if (day > today) return false
        val row = dao.insertCompletion(
            CompletionEntity(habitId = habitId, epochDay = day, completedAt = System.currentTimeMillis(), xp = xp)
        )
        return row != -1L
    }

    suspend fun undoCompletion(habitId: Long, day: Long) {
        dao.deleteCompletion(habitId, day)
    }

    suspend fun pauseHabit(id: Long) {
        if (dao.openPause(id) != null) return
        dao.insertPause(PauseEntity(habitId = id, startDay = LocalDate.now().toEpochDay()))
        ReminderScheduler.cancel(context, id)
    }

    suspend fun resumeHabit(id: Long) {
        val open = dao.openPause(id) ?: return
        val today = LocalDate.now().toEpochDay()
        // Paused days are start..(resume day - 1). Resuming the same day it was paused leaves no paused days.
        if (today - 1 < open.startDay) dao.deletePause(open.id) else dao.updatePause(open.copy(endDay = today - 1))
        dao.getHabit(id)?.let { ReminderScheduler.schedule(context, it) }
    }

    suspend fun rescheduleAll() {
        for (h in dao.allHabits()) {
            if (dao.openPause(h.id) == null) ReminderScheduler.schedule(context, h) else ReminderScheduler.cancel(context, h.id)
        }
    }

    suspend fun loadState(): AppState = buildState(
        dao.allHabits(), dao.allCompletions(), dao.allPauses(), settings.first(), LocalDate.now().toEpochDay()
    )

    suspend fun exportJson(): String {
        val s = settings.first()
        return BackupCodec.encode(
            BackupData(dao.allHabits(), dao.allCompletions(), dao.allPauses(), s.xpBanked, s.unlocked)
        )
    }

    /** Replaces ALL current data with the backup. Throws if the file is invalid (nothing is changed then). */
    suspend fun importJson(json: String) {
        val data = BackupCodec.decode(json)
        val oldIds = dao.allHabits().map { it.id }
        db.withTransaction {
            dao.clearCompletions()
            dao.clearPauses()
            dao.clearHabits()
            dao.insertHabits(data.habits)
            dao.insertCompletions(data.completions)
            dao.insertPauses(data.pauses)
        }
        oldIds.forEach { ReminderScheduler.cancel(context, it) }
        settingsStore.replaceProgress(data.xpBanked, data.unlocked)
        rescheduleAll()
    }
}
