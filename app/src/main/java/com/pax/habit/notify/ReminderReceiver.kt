package com.pax.habit.notify

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.pax.habit.MainActivity
import com.pax.habit.PaxHabitApp
import com.pax.habit.R
import com.pax.habit.domain.Schedule
import com.pax.habit.domain.StreakEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(ReminderScheduler.EXTRA_HABIT_ID, -1L)
        if (habitId < 0) return
        val app = context.applicationContext as PaxHabitApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handle(app, habitId)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handle(app: PaxHabitApp, habitId: Long) {
        val dao = app.repository.db.dao()
        val habit = dao.getHabit(habitId) ?: return
        val today = LocalDate.now().toEpochDay()
        val pauses = dao.pausesFor(habitId)
        val done = dao.completionDays(habitId).toSet()
        val paused = StreakEngine.isPaused(pauses, today)

        if (!paused && Schedule.isScheduled(habit, today) && today !in done) {
            val streak = StreakEngine.compute(habit, done, pauses, today).current
            val text = when {
                habit.goalValue != null -> "Goal: ${habit.goalValue} ${habit.goalUnit}".trim()
                streak > 0 -> "🔥 $streak day streak. Keep it going."
                else -> "Time to check this one off."
            }
            show(app, habitId.toInt(), habit.name, text)
        }
        // A paused habit gets no reminders; resuming it schedules the alarm again.
        if (!paused) ReminderScheduler.schedule(app, habit)
    }

    @SuppressLint("MissingPermission")
    private fun show(app: PaxHabitApp, id: Int, title: String, text: String) {
        val allowed = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!allowed) return
        val open = PendingIntent.getActivity(
            app,
            id,
            Intent(app, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(app, PaxHabitApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setColor(0xFF4F2FDB.toInt())
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(app).notify(id, notification)
    }
}
