package com.pax.habit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Room
import com.pax.habit.data.AppDatabase
import com.pax.habit.data.HabitRepository
import com.pax.habit.data.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaxHabitApp : Application() {
    lateinit var repository: HabitRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(this, AppDatabase::class.java, "pax_habit.db").build()
        repository = HabitRepository(this, db, SettingsStore(this))

        val channel = NotificationChannel(CHANNEL_ID, "Habit reminders", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "Reminders for the habits you schedule"
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        // Keeps alarms correct after app updates, time zone and clock changes.
        CoroutineScope(Dispatchers.IO).launch { repository.rescheduleAll() }
    }

    companion object {
        const val CHANNEL_ID = "habit_reminders"
    }
}
