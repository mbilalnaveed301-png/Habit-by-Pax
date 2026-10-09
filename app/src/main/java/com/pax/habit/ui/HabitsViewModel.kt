package com.pax.habit.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pax.habit.PaxHabitApp
import com.pax.habit.data.HabitEntity
import com.pax.habit.domain.AppState
import com.pax.habit.domain.Achievements
import com.pax.habit.domain.ProgressSnapshot
import com.pax.habit.domain.Progression
import com.pax.habit.domain.Schedule
import com.pax.habit.domain.StreakEngine
import com.pax.habit.domain.buildState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class UiEvent(val message: String)

class HabitsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as PaxHabitApp).repository

    /** Re-evaluated every few seconds so the whole UI rolls over at midnight and follows date changes. */
    private val todayFlow = flow {
        while (true) {
            emit(LocalDate.now().toEpochDay())
            delay(15_000)
        }
    }.distinctUntilChanged()

    val state: StateFlow<AppState> = combine(
        repo.habits, repo.completions, repo.pauses, repo.settings, todayFlow
    ) { habits, completions, pauses, settings, today ->
        buildState(habits, completions, pauses, settings, today)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppState())

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    private fun say(message: String) {
        _events.tryEmit(UiEvent(message))
    }

    fun toggleToday(habitId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val today = LocalDate.now().toEpochDay()
                val ui = state.value.habits.firstOrNull { it.habit.id == habitId } ?: return@launch
                val xpBefore = state.value.totalXp
                if (ui.completions.containsKey(today)) {
                    repo.undoCompletion(habitId, today)
                    return@launch
                }
                if (!Schedule.isScheduled(ui.habit, today) || StreakEngine.isPaused(ui.pauses, today)) return@launch
                val newStreak = StreakEngine.compute(ui.habit, ui.completions.keys + today, ui.pauses, today).current
                val xp = Progression.xpForCompletion(newStreak)
                if (repo.completeHabit(habitId, today, xp)) checkProgress(xpBefore)
            } catch (e: Exception) {
                say("Couldn't save that. Please try again.")
            }
        }
    }

    private suspend fun checkProgress(xpBefore: Int) {
        val st = repo.loadState()
        val snapshot = ProgressSnapshot(
            totalCompletions = st.totalCompletions,
            bestHabitStreak = st.habits.maxOfOrNull { it.stats.best } ?: 0,
            habitCount = st.habits.size,
            level = st.level,
            perfectDay = st.summary.total >= 3 && st.summary.done == st.summary.total
        )
        val newly = Achievements.evaluate(snapshot) - st.settings.unlocked
        if (newly.isNotEmpty()) {
            repo.settingsStore.addUnlocked(newly)
            newly.forEach { id -> Achievements.byId(id)?.let { say("🏆 Achievement unlocked: ${it.title}") } }
        }
        if (st.level > Progression.level(xpBefore)) say("🎉 Level up! You're now level ${st.level}")
    }

    fun saveHabit(habit: HabitEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isNew = habit.id == 0L
                repo.saveHabit(habit)
                if (isNew) checkProgress(state.value.totalXp)
            } catch (e: Exception) {
                say("Couldn't save the habit.")
            }
        }
    }

    fun deleteHabit(id: Long) {
        viewModelScope.launch(Dispatchers.IO) { repo.deleteHabit(id) }
    }

    fun pauseHabit(id: Long) {
        viewModelScope.launch(Dispatchers.IO) { repo.pauseHabit(id) }
    }

    fun resumeHabit(id: Long) {
        viewModelScope.launch(Dispatchers.IO) { repo.resumeHabit(id) }
    }

    fun setTheme(value: String) {
        viewModelScope.launch { repo.settingsStore.setTheme(value) }
    }

    fun setDynamicColor(value: Boolean) {
        viewModelScope.launch { repo.settingsStore.setDynamicColor(value) }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = repo.exportJson()
                val out = getApplication<Application>().contentResolver.openOutputStream(uri, "wt")
                    ?: throw IllegalStateException("Cannot open file")
                out.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                say("Backup saved")
            } catch (e: Exception) {
                say("Backup failed. Try another location.")
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = getApplication<Application>().contentResolver.openInputStream(uri)
                    ?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: throw IllegalStateException("Cannot open file")
                repo.importJson(json)
                say("Backup restored")
            } catch (e: Exception) {
                say("That file isn't a valid Pax Habit backup.")
            }
        }
    }
}
