package com.pax.habit.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pax.habit.PaxHabitApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Alarms are cleared on reboot and app updates, so schedule them again. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as PaxHabitApp
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.repository.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }
}
