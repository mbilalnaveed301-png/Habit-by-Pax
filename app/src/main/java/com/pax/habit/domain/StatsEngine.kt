package com.pax.habit.domain

import com.pax.habit.data.HabitEntity
import kotlin.math.ceil

data class StatsResult(
    val overallRate: Float,
    val totalCompletions: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val xp: Int,
    val averageRate: Float,
    val most: Pair<HabitEntity, Float>?,
    val least: Pair<HabitEntity, Float>?,
    /** Completion rate per bucket (a bucket is one day, or several days for long ranges). */
    val bars: List<Float>,
    /** Completion rate per weekday, Monday first. Null when no habit was scheduled on that weekday. */
    val weekday: List<Float?>,
    val fromDay: Long,
    val toDay: Long
)

enum class StatsRange(val label: String, val days: Int?) {
    D7("7 Days", 7), D30("30 Days", 30), D90("90 Days", 90), ALL("All Time", null)
}

object StatsEngine {
    data class DayRate(val scheduled: Int, val done: Int)

    /** How many habits were active on [day], and how many of those were completed. */
    fun dayRate(habits: List<HabitUi>, day: Long, today: Long, countPendingToday: Boolean): DayRate {
        var s = 0
        var d = 0
        for (u in habits) {
            if (!StreakEngine.isActive(u.habit, u.pauses, day)) continue
            val done = u.completions.containsKey(day)
            if (day == today && !done && !countPendingToday) continue
            s++
            if (done) d++
        }
        return DayRate(s, d)
    }

    /** (current, best) streak of "every habit scheduled that day was completed". */
    fun overallStreaks(habits: List<HabitUi>, today: Long): Pair<Int, Int> {
        if (habits.isEmpty()) return 0 to 0
        val lower = maxOf(habits.minOf { it.habit.startDay }, today - 3650)
        if (lower > today) return 0 to 0

        var current = 0
        var d = today
        while (d >= lower) {
            val r = dayRate(habits, d, today, true)
            if (r.scheduled > 0) {
                if (r.done == r.scheduled) current++ else if (d != today) break
            }
            d--
        }

        var run = 0
        var best = 0
        for (day in lower..today) {
            val r = dayRate(habits, day, today, true)
            if (r.scheduled == 0) continue
            if (r.done == r.scheduled) {
                run++
                if (run > best) best = run
            } else if (day != today) {
                run = 0
            }
        }
        return current to best
    }

    fun rangeStart(range: StatsRange, habits: List<HabitUi>, today: Long): Long {
        val days = range.days
        if (days != null) return today - (days - 1)
        val first = habits.minOfOrNull {
            minOf(it.habit.startDay, it.completions.keys.minOrNull() ?: Long.MAX_VALUE)
        } ?: today
        return maxOf(minOf(first, today), today - 3650)
    }

    fun compute(range: StatsRange, habits: List<HabitUi>, today: Long): StatsResult {
        val from = rangeStart(range, habits, today)
        var scheduled = 0
        var done = 0
        var rateSum = 0f
        var rateDays = 0
        val daily = ArrayList<DayRate>()
        val wdS = IntArray(7)
        val wdD = IntArray(7)
        for (day in from..today) {
            val r = dayRate(habits, day, today, false)
            daily.add(r)
            scheduled += r.scheduled
            done += r.done
            if (r.scheduled > 0) {
                rateSum += r.done.toFloat() / r.scheduled
                rateDays++
            }
            val wd = Schedule.dayBit(day)
            wdS[wd] += r.scheduled
            wdD[wd] += r.done
        }

        var completions = 0
        var xp = 0
        for (u in habits) {
            for ((day, x) in u.completions) {
                if (day in from..today) {
                    completions++
                    xp += x
                }
            }
        }

        val perHabit = habits.mapNotNull { u ->
            var s = 0
            var c = 0
            for (day in from..today) {
                if (!StreakEngine.isActive(u.habit, u.pauses, day)) continue
                val dn = u.completions.containsKey(day)
                if (day == today && !dn) continue
                s++
                if (dn) c++
            }
            if (s == 0) null else u.habit to (c.toFloat() / s)
        }

        val bucket = ceil(daily.size / 30.0).toInt().coerceAtLeast(1)
        val bars = daily.chunked(bucket).map { ch ->
            val s = ch.sumOf { it.scheduled }
            if (s == 0) 0f else ch.sumOf { it.done }.toFloat() / s
        }
        val (cur, best) = overallStreaks(habits, today)
        return StatsResult(
            overallRate = if (scheduled == 0) 0f else done.toFloat() / scheduled,
            totalCompletions = completions,
            currentStreak = cur,
            bestStreak = best,
            xp = xp,
            averageRate = if (rateDays == 0) 0f else rateSum / rateDays,
            most = perHabit.maxByOrNull { it.second },
            least = if (perHabit.size > 1) perHabit.minByOrNull { it.second } else null,
            bars = bars,
            weekday = (0..6).map { if (wdS[it] == 0) null else wdD[it].toFloat() / wdS[it] },
            fromDay = from,
            toDay = today
        )
    }

    /** day -> fraction of scheduled habits completed (only days with something scheduled). */
    fun dailyFractions(habits: List<HabitUi>, from: Long, today: Long): Map<Long, Float> {
        val map = HashMap<Long, Float>()
        for (day in from..today) {
            val r = dayRate(habits, day, today, false)
            if (r.scheduled > 0) map[day] = r.done.toFloat() / r.scheduled
        }
        return map
    }
}
