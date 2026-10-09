package com.pax.habit.domain

import com.pax.habit.data.AppSettings
import com.pax.habit.data.CompletionEntity
import com.pax.habit.data.HabitEntity
import com.pax.habit.data.PauseEntity
import java.time.LocalDate

data class HabitUi(
    val habit: HabitEntity,
    /** epochDay -> XP earned for that completion. */
    val completions: Map<Long, Int>,
    val pauses: List<PauseEntity>,
    val stats: HabitStats,
    val completedToday: Boolean,
    /** Scheduled today AND not paused. Only these count towards today's progress. */
    val scheduledToday: Boolean,
    val paused: Boolean
)

data class TodaySummary(
    val total: Int = 0,
    val done: Int = 0,
    val xpToday: Int = 0,
    val streak: Int = 0
) {
    val fraction: Float get() = if (total == 0) 0f else done.toFloat() / total
}

data class AppState(
    val loaded: Boolean = false,
    val today: Long = LocalDate.now().toEpochDay(),
    val habits: List<HabitUi> = emptyList(),
    val summary: TodaySummary = TodaySummary(),
    val totalXp: Int = 0,
    val level: Int = 1,
    val totalCompletions: Int = 0,
    val settings: AppSettings = AppSettings()
)

fun buildState(
    habits: List<HabitEntity>,
    completions: List<CompletionEntity>,
    pauses: List<PauseEntity>,
    settings: AppSettings,
    today: Long
): AppState {
    val compsBy = completions.groupBy { it.habitId }
    val pausesBy = pauses.groupBy { it.habitId }
    val uis = habits.map { h ->
        val c = (compsBy[h.id] ?: emptyList()).associate { it.epochDay to it.xp }
        val p = pausesBy[h.id] ?: emptyList()
        val paused = StreakEngine.isPaused(p, today)
        HabitUi(
            habit = h,
            completions = c,
            pauses = p,
            stats = StreakEngine.compute(h, c.keys, p, today),
            completedToday = today in c,
            scheduledToday = Schedule.isScheduled(h, today) && !paused,
            paused = paused
        )
    }
    val total = uis.count { it.scheduledToday }
    val done = uis.count { it.scheduledToday && it.completedToday }
    val xpToday = uis.sumOf { it.completions[today] ?: 0 }
    val totalXp = completions.sumOf { it.xp } + settings.xpBanked
    return AppState(
        loaded = true,
        today = today,
        habits = uis,
        summary = TodaySummary(total, done, xpToday, StatsEngine.overallStreaks(uis, today).first),
        totalXp = totalXp,
        level = Progression.level(totalXp),
        totalCompletions = completions.size,
        settings = settings
    )
}
