@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.pax.habit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pax.habit.domain.AppState
import com.pax.habit.domain.HabitUi
import com.pax.habit.domain.Schedule
import com.pax.habit.ui.components.EmptyState
import com.pax.habit.ui.components.HabitIconBadge
import com.pax.habit.ui.components.PaxCard
import com.pax.habit.ui.components.PaxIcons
import com.pax.habit.ui.streakLabel
import com.pax.habit.ui.theme.PaxSuccess
import kotlin.math.roundToInt

private enum class HabitFilter(val label: String) {
    ALL("All"), ACTIVE("Active"), PAUSED("Paused"), DONE("Completed Today"), TODO("Incomplete Today")
}

private enum class HabitSort(val label: String) {
    NAME("Name"), STREAK("Streak"), RATE("Completion rate"), NEWEST("Newest")
}

@Composable
fun HabitsScreen(state: AppState, onOpenHabit: (Long) -> Unit, onAddHabit: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var filterIdx by rememberSaveable { mutableIntStateOf(0) }
    var sortIdx by rememberSaveable { mutableIntStateOf(HabitSort.NEWEST.ordinal) }
    var sortMenu by remember { mutableStateOf(false) }
    val filter = HabitFilter.entries[filterIdx]
    val sort = HabitSort.entries[sortIdx]

    val items = remember(state.habits, query, filter, sort) {
        val q = query.trim()
        val filtered = state.habits.filter { ui ->
            val matches = q.isEmpty() ||
                ui.habit.name.contains(q, ignoreCase = true) ||
                ui.habit.description.contains(q, ignoreCase = true)
            val inFilter = when (filter) {
                HabitFilter.ALL -> true
                HabitFilter.ACTIVE -> !ui.paused
                HabitFilter.PAUSED -> ui.paused
                HabitFilter.DONE -> ui.completedToday
                HabitFilter.TODO -> ui.scheduledToday && !ui.completedToday
            }
            matches && inFilter
        }
        when (sort) {
            HabitSort.NAME -> filtered.sortedBy { it.habit.name.lowercase() }
            HabitSort.STREAK -> filtered.sortedByDescending { it.stats.current }
            HabitSort.RATE -> filtered.sortedByDescending { it.stats.rate }
            HabitSort.NEWEST -> filtered.sortedByDescending { it.habit.createdAt }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Habits", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
                Box {
                    IconButton(onClick = { sortMenu = true }) { Icon(PaxIcons.Sort, contentDescription = "Sort habits") }
                    DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                        HabitSort.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                leadingIcon = { RadioButton(selected = option == sort, onClick = null) },
                                onClick = {
                                    sortIdx = option.ordinal
                                    sortMenu = false
                                }
                            )
                        }
                    }
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                placeholder = { Text("Search habits") },
                leadingIcon = { Icon(PaxIcons.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) { Icon(PaxIcons.Close, contentDescription = "Clear search") }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HabitFilter.entries.toList()) { f ->
                    FilterChip(
                        selected = f == filter,
                        onClick = { filterIdx = f.ordinal },
                        label = { Text(f.label) }
                    )
                }
            }
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (items.isEmpty()) {
                    item {
                        if (state.habits.isEmpty()) {
                            EmptyState("No habits yet", "Tap Add habit to create your first one.")
                        } else {
                            EmptyState("No matches", "Try a different search or filter.")
                        }
                    }
                } else {
                    items(items, key = { it.habit.id }) { ui -> HabitListCard(ui) { onOpenHabit(ui.habit.id) } }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onAddHabit,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = { Icon(PaxIcons.Add, contentDescription = null) },
            text = { Text("Add habit") }
        )
    }
}

@Composable
private fun HabitListCard(ui: HabitUi, onClick: () -> Unit) {
    val h = ui.habit
    PaxCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HabitIconBadge(h.iconKey, Color(h.colorArgb))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(h.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${Schedule.label(h)} · ${(ui.stats.rate * 100).roundToInt()}% done",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (ui.stats.current > 0) "🔥 ${streakLabel(ui.stats.current)}" else "No streak yet",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            when {
                ui.paused -> StatusPill("Paused", MaterialTheme.colorScheme.onSurfaceVariant)
                ui.completedToday -> StatusPill("Done today", PaxSuccess)
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Surface(shape = CircleShape, color = color.copy(alpha = 0.14f)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}
