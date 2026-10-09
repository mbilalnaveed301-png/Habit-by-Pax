@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.pax.habit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pax.habit.domain.AppState
import com.pax.habit.domain.DayState
import com.pax.habit.domain.HabitUi
import com.pax.habit.domain.Schedule
import com.pax.habit.domain.StreakEngine
import com.pax.habit.ui.components.Heatmap
import com.pax.habit.ui.components.HabitIconBadge
import com.pax.habit.ui.components.MonthCalendar
import com.pax.habit.ui.components.PaxCard
import com.pax.habit.ui.components.PaxIcons
import com.pax.habit.ui.components.SectionTitle
import com.pax.habit.ui.components.StatTile
import com.pax.habit.ui.formatMinutes
import com.pax.habit.ui.goalLabel
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

@Composable
fun DetailScreen(
    habitId: Long,
    state: AppState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    val ui = state.habits.firstOrNull { it.habit.id == habitId }
    if (ui == null) {
        Box(Modifier.fillMaxSize())
        return
    }
    val h = ui.habit
    val color = Color(h.colorArgb)
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val month = YearMonth.from(LocalDate.ofEpochDay(state.today)).plusMonths(monthOffset.toLong())

    val weekStart = state.today - Schedule.dayBit(state.today)
    val monthStart = LocalDate.ofEpochDay(state.today).withDayOfMonth(1).toEpochDay()
    val week = periodCount(ui, weekStart, state.today)
    val monthly = periodCount(ui, monthStart, state.today)
    val heat = remember(ui, state.today) {
        val map = HashMap<Long, Float>()
        for (day in (state.today - 18 * 7)..state.today) {
            when (StreakEngine.dayState(ui.habit, ui.completions.keys, ui.pauses, day, state.today)) {
                DayState.COMPLETED -> map[day] = 1f
                DayState.MISSED -> map[day] = 0f
                else -> Unit
            }
        }
        map as Map<Long, Float>
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Habit details") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(PaxIcons.Back, contentDescription = "Back") } }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PaxCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HabitIconBadge(h.iconKey, color, boxSize = 64.dp)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(h.name, style = MaterialTheme.typography.headlineSmall)
                        if (h.description.isNotBlank()) {
                            Text(
                                h.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                val facts = buildList {
                    add(Schedule.label(h))
                    h.reminderMinutes?.let { add("Reminder " + formatMinutes(it)) }
                    goalLabel(h)?.let { add("Goal: $it") }
                }
                Text(
                    facts.joinToString("  ·  "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (ui.paused) {
                Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        "Paused. These days don't count as missed, your streak is safe and reminders are off.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Current streak", "${ui.stats.current} days", Modifier.weight(1f))
                StatTile("Best streak", "${ui.stats.best} days", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Total completions", "${ui.stats.total}", Modifier.weight(1f))
                StatTile("Completion", "${(ui.stats.rate * 100).roundToInt()}%", Modifier.weight(1f))
            }

            PaxCard(Modifier.fillMaxWidth()) {
                SectionTitle("Performance")
                Spacer(Modifier.height(12.dp))
                PerformanceRow("This week", week, color)
                Spacer(Modifier.height(14.dp))
                PerformanceRow("This month", monthly, color)
            }

            PaxCard(Modifier.fillMaxWidth()) {
                MonthCalendar(
                    habit = h,
                    completions = ui.completions.keys,
                    pauses = ui.pauses,
                    today = state.today,
                    month = month,
                    accent = color,
                    onPrev = { monthOffset-- },
                    onNext = { monthOffset++ }
                )
            }

            PaxCard(Modifier.fillMaxWidth()) {
                SectionTitle("Recent activity")
                Spacer(Modifier.height(12.dp))
                Heatmap(heat, state.today, weeks = 18, color = color)
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) {
                    Icon(PaxIcons.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Edit")
                }
                FilledTonalButton(onClick = if (ui.paused) onResume else onPause) {
                    Icon(if (ui.paused) PaxIcons.Play else PaxIcons.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (ui.paused) "Resume" else "Pause")
                }
                TextButton(
                    onClick = { confirmDelete = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(PaxIcons.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this habit?") },
            text = { Text("\"${h.name}\" and its history will be removed for good. The XP you earned stays.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

/** (done, scheduled) for the days from..today. Paused and unscheduled days are left out. */
private fun periodCount(ui: HabitUi, from: Long, today: Long): Pair<Int, Int> {
    var scheduled = 0
    var done = 0
    for (d in from..today) {
        if (!StreakEngine.isActive(ui.habit, ui.pauses, d)) continue
        scheduled++
        if (ui.completions.containsKey(d)) done++
    }
    return done to scheduled
}

@Composable
private fun PerformanceRow(label: String, count: Pair<Int, Int>, color: Color) {
    val (done, scheduled) = count
    val fraction = if (scheduled == 0) 0f else done.toFloat() / scheduled
    Column {
        Row {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(
                if (scheduled == 0) "Nothing scheduled" else "$done of $scheduled · ${(fraction * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}
