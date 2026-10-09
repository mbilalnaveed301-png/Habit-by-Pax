package com.pax.habit.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pax.habit.data.HabitEntity
import com.pax.habit.domain.Schedule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * One alarm per habit, always pointing at the next scheduled occurrence.
 * AlarmManager is used (not WorkManager) because reminders must fire at a chosen clock time.
 * setAndAllowWhileIdle needs no special permission and still fires in Doze, a few minutes late at worst.
 */
object ReminderScheduler {
    const val EXTRA_HABIT_ID = "habit_id"

    fun schedule(context: Context, habit: HabitEntity) {
        cancel(context, habit.id)
        val minutes = habit.reminderMinutes ?: return
        val trigger = nextTrigger(habit, minutes, LocalDateTime.now()) ?: return
        val millis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val am = context.getSystemService(AlarmManager::class.java)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent(context, habit.id))
    }

    fun cancel(context: Context, habitId: Long) {
        val am = context.getSystemService(AlarmManager::class.java)
        am.cancel(pendingIntent(context, habitId))
    }

    fun nextTrigger(h: HabitEntity, minutes: Int, now: LocalDateTime): LocalDateTime? {
        val today = now.toLocalDate().toEpochDay()
        for (i in 0..800) {
            val day = today + i
            if (!Schedule.isScheduled(h, day)) continue
            val t = LocalDate.ofEpochDay(day).atTime(minutes / 60, minutes % 60)
            if (t.isAfter(now)) return t
        }
        return null
    }

    private fun pendingIntent(context: Context, habitId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_HABIT_ID, habitId)
        return PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
