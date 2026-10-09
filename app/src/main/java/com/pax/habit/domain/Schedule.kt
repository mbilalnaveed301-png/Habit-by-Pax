package com.pax.habit.domain

import com.pax.habit.data.HabitEntity
import com.pax.habit.data.SCHEDULE_INTERVAL
import com.pax.habit.data.SCHEDULE_WEEKDAYS
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

object Schedule {
    const val ALL_DAYS = 127
    const val WEEKDAYS = 0b0011111 // Mon-Fri
    const val WEEKEND = 0b1100000 // Sat-Sun

    /** 0 = Monday ... 6 = Sunday. */
    fun dayBit(day: Long): Int = LocalDate.ofEpochDay(day).dayOfWeek.value - 1

    /** Is the habit scheduled on this day (ignores pauses)? Days before the start date never are. */
    fun isScheduled(h: HabitEntity, day: Long): Boolean {
        if (day < h.startDay) return false
        return when (h.scheduleType) {
            SCHEDULE_WEEKDAYS -> ((h.daysMask shr dayBit(day)) and 1) == 1
            SCHEDULE_INTERVAL -> (day - h.startDay) % maxOf(1, h.intervalDays) == 0L
            else -> true
        }
    }

    fun label(h: HabitEntity): String = when (h.scheduleType) {
        SCHEDULE_WEEKDAYS -> maskLabel(h.daysMask)
        SCHEDULE_INTERVAL -> if (h.intervalDays <= 1) "Every day" else "Every ${h.intervalDays} days"
        else -> "Every day"
    }

    fun maskLabel(mask: Int): String {
        val m = mask and ALL_DAYS
        val count = Integer.bitCount(m)
        return when {
            m == ALL_DAYS -> "Every day"
            m == WEEKDAYS -> "Weekdays"
            m == WEEKEND -> "Weekends"
            m == 0 -> "No days selected"
            count >= 4 -> "$count days per week"
            else -> (0..6).filter { ((m shr it) and 1) == 1 }.joinToString(" · ") {
                DayOfWeek.of(it + 1).getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
        }
    }
}
