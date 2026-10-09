package com.pax.habit.domain

import com.pax.habit.data.HabitEntity
import com.pax.habit.data.PauseEntity

enum class DayState { COMPLETED, MISSED, SCHEDULED, NOT_SCHEDULED, PAUSED }

data class HabitStats(
    val current: Int = 0,
    val best: Int = 0,
    val total: Int = 0,
    /** Completed / scheduled days so far (today only counts once it is done). */
    val rate: Float = 0f,
    val scheduledDays: Int = 0,
    val completedScheduledDays: Int = 0
)

/**
 * Streaks are always recomputed from the real completion dates + the schedule + pauses.
 * Nothing is ever "incremented", so editing, restoring a backup or changing the device date
 * can never leave a stale counter behind.
 *
 * Rules:
 *  - A day that is not scheduled (or is paused, or is before the start date) neither counts nor breaks a streak.
 *  - A scheduled, unpaused day in the past without a completion breaks it.
 *  - Today never breaks a streak: it is still pending until the day is over.
 *  - Completions dated after today (clock was changed) are ignored.
 */
object StreakEngine {
    fun isPaused(pauses: List<PauseEntity>, day: Long): Boolean =
        pauses.any { day >= it.startDay && (it.endDay == null || day <= it.endDay) }

    fun isActive(h: HabitEntity, pauses: List<PauseEntity>, day: Long): Boolean =
        Schedule.isScheduled(h, day) && !isPaused(pauses, day)

    fun dayState(h: HabitEntity, completions: Set<Long>, pauses: List<PauseEntity>, day: Long, today: Long): DayState =
        when {
            day in completions -> DayState.COMPLETED
            day < h.startDay -> DayState.NOT_SCHEDULED
            isPaused(pauses, day) ->
                if (Schedule.isScheduled(h, day)) DayState.PAUSED else DayState.NOT_SCHEDULED
            !Schedule.isScheduled(h, day) -> DayState.NOT_SCHEDULED
            day < today -> DayState.MISSED
            else -> DayState.SCHEDULED
        }

    fun compute(h: HabitEntity, completions: Set<Long>, pauses: List<PauseEntity>, today: Long): HabitStats {
        val done: Set<Long> =
            if (completions.all { it <= today }) completions else completions.filterTo(HashSet()) { it <= today }
        val lower = minOf(h.startDay, done.minOrNull() ?: h.startDay)
        if (lower > today) return HabitStats()

        // Current streak: walk backwards from today.
        var current = 0
        var d = today
        while (d >= lower) {
            if (d in done) {
                current++
            } else if (d != today && isActive(h, pauses, d)) {
                break
            }
            d--
        }

        // Best streak + completion rate: walk forwards once.
        var run = 0
        var best = 0
        var scheduled = 0
        var scheduledDone = 0
        for (day in lower..today) {
            val completed = day in done
            val active = isActive(h, pauses, day)
            if (completed) {
                run++
                if (run > best) best = run
            } else if (active && day != today) {
                run = 0
            }
            if (active && (day != today || completed)) {
                scheduled++
                if (completed) scheduledDone++
            }
        }
        val rate = if (scheduled == 0) 0f else scheduledDone.toFloat() / scheduled
        return HabitStats(current, best, done.size, rate, scheduled, scheduledDone)
    }
}
