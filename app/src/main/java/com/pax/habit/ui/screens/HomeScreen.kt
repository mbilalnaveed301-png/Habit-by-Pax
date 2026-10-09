@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.pax.habit.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pax.habit.domain.AppState
import com.pax.habit.domain.HabitUi
import com.pax.habit.domain.TodaySummary
import com.pax.habit.ui.components.EmptyState
import com.pax.habit.ui.components.HabitIconBadge
import com.pax.habit.ui.components.PaxCard
import com.pax.habit.ui.components.PaxIcons
import com.pax.habit.ui.components.ProgressRing
import com.pax.habit.ui.components.SectionTitle
import com.pax.habit.ui.formatMinutes
import com.pax.habit.ui.goalLabel
import com.pax.habit.ui.streakLabel
import com.pax.habit.ui.theme.PaxGradient
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun HomeScreen(
    state: AppState,
    onToggle: (Long) -> Unit,
    onOpenHabit: (Long) -> Unit,
    onAddHabit: () -> Unit
) {
    val hour by produceState(initialValue = LocalTime.now().hour) {
        while (true) {
            delay(60_000)
            value = LocalTime.now().hour
        }
    }
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
    val date = remember(state.today) {
        LocalDate.ofEpochDay(state.today).format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    }
    val todays = remember(state.habits) {
        state.habits.filter { it.scheduledToday }
            .sortedWith(compareBy({ it.habit.reminderMinutes ?: Int.MAX_VALUE }, { it.habit.name.lowercase() }))
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.padding(bottom = 4.dp)) {
                Text("$greeting 👋", style = MaterialTheme.typography.headlineLarge)
                Text(date, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { HeroCard(state.summary) }
        item { SectionTitle("Today's Habits", Modifier.padding(top = 8.dp)) }
        when {
            state.habits.isEmpty() -> item {
                EmptyState(
                    title = "No habits yet",
                    message = "Create your first habit and start a streak today."
                ) { Button(onClick = onAddHabit) { Text("Add a habit") } }
            }
            todays.isEmpty() -> item {
                EmptyState(
                    title = "Nothing scheduled today",
                    message = "Enjoy the rest day. Your next habit shows up here when it's due."
                )
            }
            else -> items(todays, key = { it.habit.id }) { ui ->
                TodayHabitCard(ui, onToggle = { onToggle(ui.habit.id) }, onOpen = { onOpenHabit(ui.habit.id) })
            }
        }
    }
}

@Composable
private fun HeroCard(s: TodaySummary) {
    val pct by animateIntAsState((s.fraction * 100).roundToInt(), tween(700), label = "pct")
    val xp by animateIntAsState(s.xpToday, tween(500), label = "xp")
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(PaxGradient)
            .padding(22.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("TODAY'S PROGRESS", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
                    Text("$pct%", style = MaterialTheme.typography.displayMedium, color = Color.White)
                    Text(
                        if (s.total == 0) "No habits scheduled today" else "${s.done} of ${s.total} habits completed",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
                ProgressRing(
                    progress = s.fraction,
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f),
                    diameter = 96.dp,
                    strokeWidth = 10.dp
                ) {
                    Text("${s.done}/${s.total}", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
            Spacer(Modifier.size(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroChip("⚡ +$xp XP")
                HeroChip("🔥 ${streakLabel(s.streak)}")
            }
        }
    }
}

@Composable
private fun HeroChip(text: String) {
    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.18f)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
    }
}

@Composable
private fun TodayHabitCard(ui: HabitUi, onToggle: () -> Unit, onOpen: () -> Unit) {
    val h = ui.habit
    val color = Color(h.colorArgb)
    PaxCard(Modifier.fillMaxWidth(), onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HabitIconBadge(h.iconKey, color)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(h.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (h.description.isNotBlank()) {
                    Text(
                        h.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.size(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    h.reminderMinutes?.let { Meta(PaxIcons.Clock, formatMinutes(it), MaterialTheme.colorScheme.onSurfaceVariant) }
                    Meta(
                        PaxIcons.Fire,
                        if (ui.stats.current > 0) streakLabel(ui.stats.current) else "No streak yet",
                        if (ui.stats.current > 0) Color(0xFFF26B38) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    goalLabel(h)?.let { Meta(PaxIcons.Flag, it, MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            Spacer(Modifier.width(8.dp))
            CompleteButton(ui.completedToday, color, onToggle)
        }
    }
}

@Composable
private fun Meta(icon: ImageVector, text: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun CompleteButton(done: Boolean, color: Color, onToggle: () -> Unit) {
    val p by animateFloatAsState(if (done) 1f else 0f, tween(260), label = "complete")
    val scale = 1f + 0.18f * sin(PI * p).toFloat()
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val idleIcon = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val haptic = LocalHapticFeedback.current
    Box(
        Modifier
            .size(46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(lerp(track, color, p))
            .toggleable(
                value = done,
                role = Role.Checkbox,
                onValueChange = {
                    if (!done) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            PaxIcons.Check,
            contentDescription = if (done) "Completed. Tap to undo" else "Mark as done",
            tint = lerp(idleIcon, Color.White, p)
        )
    }
}
