@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.pax.habit.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pax.habit.data.HabitEntity
import com.pax.habit.data.PauseEntity
import com.pax.habit.domain.DayState
import com.pax.habit.domain.StreakEngine
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.min

/** Every icon the app uses, in one place. */
object PaxIcons {
    val Add: ImageVector = Icons.Filled.Add
    val Back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val Prev: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft
    val Next: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight
    val Sort: ImageVector = Icons.AutoMirrored.Filled.Sort
    val Check: ImageVector = Icons.Filled.Check
    val Close: ImageVector = Icons.Filled.Close
    val Search: ImageVector = Icons.Filled.Search
    val Delete: ImageVector = Icons.Filled.Delete
    val Edit: ImageVector = Icons.Filled.Edit
    val Pause: ImageVector = Icons.Filled.Pause
    val Play: ImageVector = Icons.Filled.PlayArrow
    val Home: ImageVector = Icons.Filled.Home
    val Habits: ImageVector = Icons.Filled.Checklist
    val Stats: ImageVector = Icons.Filled.BarChart
    val Profile: ImageVector = Icons.Filled.Person
    val Fire: ImageVector = Icons.Filled.LocalFireDepartment
    val Trophy: ImageVector = Icons.Filled.EmojiEvents
    val Bell: ImageVector = Icons.Filled.Notifications
    val Backup: ImageVector = Icons.Filled.Backup
    val Restore: ImageVector = Icons.Filled.Restore
    val Palette: ImageVector = Icons.Filled.Palette
    val Info: ImageVector = Icons.Filled.Info
    val Privacy: ImageVector = Icons.Filled.PrivacyTip
    val Mail: ImageVector = Icons.Filled.Email
    val Bolt: ImageVector = Icons.Filled.Bolt
    val Lock: ImageVector = Icons.Filled.Lock
    val Filter: ImageVector = Icons.Filled.FilterList
    val Clock: ImageVector = Icons.Filled.Schedule
    val Calendar: ImageVector = Icons.Filled.CalendarMonth
    val Flag: ImageVector = Icons.Filled.Flag
    val Minus: ImageVector = Icons.Filled.Remove
    val Star: ImageVector = Icons.Filled.Star

    val School: ImageVector = Icons.Filled.School
    val Book: ImageVector = Icons.Filled.MenuBook
    val Water: ImageVector = Icons.Filled.WaterDrop
    val Exercise: ImageVector = Icons.Filled.FitnessCenter
    val Running: ImageVector = Icons.Filled.DirectionsRun
    val Walking: ImageVector = Icons.Filled.DirectionsWalk
    val Sleep: ImageVector = Icons.Filled.Bedtime
    val Meditation: ImageVector = Icons.Filled.SelfImprovement
    val Coding: ImageVector = Icons.Filled.Code
    val Reading: ImageVector = Icons.Filled.AutoStories
    val Music: ImageVector = Icons.Filled.MusicNote
    val Money: ImageVector = Icons.Filled.Savings
    val Health: ImageVector = Icons.Filled.Favorite
    val Food: ImageVector = Icons.Filled.Restaurant
    val Gaming: ImageVector = Icons.Filled.SportsEsports
    val Work: ImageVector = Icons.Filled.Work
}

data class HabitIconOption(val key: String, val label: String, val icon: ImageVector)

val HabitIconOptions = listOf(
    HabitIconOption("study", "Study", PaxIcons.School),
    HabitIconOption("book", "Book", PaxIcons.Book),
    HabitIconOption("water", "Water", PaxIcons.Water),
    HabitIconOption("exercise", "Exercise", PaxIcons.Exercise),
    HabitIconOption("running", "Running", PaxIcons.Running),
    HabitIconOption("walking", "Walking", PaxIcons.Walking),
    HabitIconOption("sleep", "Sleep", PaxIcons.Sleep),
    HabitIconOption("meditation", "Meditate", PaxIcons.Meditation),
    HabitIconOption("coding", "Coding", PaxIcons.Coding),
    HabitIconOption("reading", "Reading", PaxIcons.Reading),
    HabitIconOption("music", "Music", PaxIcons.Music),
    HabitIconOption("money", "Money", PaxIcons.Money),
    HabitIconOption("health", "Health", PaxIcons.Health),
    HabitIconOption("food", "Food", PaxIcons.Food),
    HabitIconOption("gaming", "Gaming", PaxIcons.Gaming),
    HabitIconOption("work", "Work", PaxIcons.Work),
    HabitIconOption("custom", "Custom", PaxIcons.Star)
)

fun iconFor(key: String): ImageVector = HabitIconOptions.firstOrNull { it.key == key }?.icon ?: PaxIcons.Star

val HabitColors: List<Int> = listOf(
    0xFF4F2FDBL, 0xFF2F6BFFL, 0xFF00A6A6L, 0xFF2E9E6AL, 0xFFE8A200L,
    0xFFF26B38L, 0xFFE5484DL, 0xFFD6409FL, 0xFF8E4EC6L, 0xFF64748BL
).map { it.toInt() }

@Composable
fun PaxCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    shape: Shape = RoundedCornerShape(24.dp),
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = color) {
            Column(Modifier.padding(padding), content = content)
        }
    } else {
        Surface(modifier = modifier, shape = shape, color = color) {
            Column(Modifier.padding(padding), content = content)
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun HabitIconBadge(iconKey: String, color: Color, modifier: Modifier = Modifier, boxSize: Dp = 48.dp) {
    Box(
        modifier
            .size(boxSize)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(iconFor(iconKey), contentDescription = null, tint = color, modifier = Modifier.size(boxSize * 0.5f))
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    diameter: Dp = 112.dp,
    strokeWidth: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "progress-ring"
    )
    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            if (animated > 0.001f) {
                drawArc(color, -90f, 360f * animated, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            }
        }
        content()
    }
}

@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier, sub: String? = null) {
    PaxCard(modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        if (sub != null) {
            Text(
                sub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier, action: @Composable (() -> Unit)? = null) {
    PaxCard(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (action != null) {
                Spacer(Modifier.height(16.dp))
                action()
            }
        }
    }
}

/** Bars from 0..1. Each bar sits on a faint full-height track. */
@Composable
fun BarChart(values: List<Float>, color: Color, modifier: Modifier = Modifier) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Canvas(modifier.fillMaxWidth().height(140.dp)) {
        if (values.isEmpty()) return@Canvas
        val gap = 4.dp.toPx()
        val barW = (size.width - gap * (values.size - 1)) / values.size
        val radius = CornerRadius(min(barW / 2f, 8.dp.toPx()))
        values.forEachIndexed { i, v ->
            val x = i * (barW + gap)
            drawRoundRect(track, Offset(x, 0f), Size(barW, size.height), radius)
            val h = size.height * v.coerceIn(0f, 1f)
            if (h > 0f) drawRoundRect(color, Offset(x, size.height - h), Size(barW, h), radius)
        }
    }
}

/**
 * Contribution-style heatmap. Columns are weeks (Monday first), the last column is the current week.
 * [fractions] maps epochDay -> 0..1; days not in the map had nothing scheduled.
 */
@Composable
fun Heatmap(fractions: Map<Long, Float>, today: Long, weeks: Int, color: Color, modifier: Modifier = Modifier) {
    val empty = MaterialTheme.colorScheme.surfaceContainerHighest
    val description = "Activity heatmap for the last $weeks weeks"
    Canvas(
        modifier
            .fillMaxWidth()
            .aspectRatio(weeks / 7f)
            .semantics { contentDescription = description }
    ) {
        val gap = 3.dp.toPx()
        val cellW = (size.width - gap * (weeks - 1)) / weeks
        val cellH = (size.height - gap * 6) / 7
        val radius = CornerRadius(min(cellW, cellH) * 0.25f)
        val todayRow = LocalDate.ofEpochDay(today).dayOfWeek.value - 1
        val lastWeekStart = today - todayRow
        for (w in 0 until weeks) {
            for (r in 0 until 7) {
                val day = lastWeekStart - (weeks - 1 - w) * 7L + r
                if (day > today) continue
                val f = fractions[day]
                val c = when {
                    f == null -> empty.copy(alpha = 0.45f)
                    f <= 0f -> empty
                    f <= 0.25f -> color.copy(alpha = 0.35f)
                    f <= 0.5f -> color.copy(alpha = 0.55f)
                    f <= 0.75f -> color.copy(alpha = 0.78f)
                    else -> color
                }
                drawRoundRect(c, Offset(w * (cellW + gap), r * (cellH + gap)), Size(cellW, cellH), radius)
            }
        }
    }
}

@Composable
fun MonthCalendar(
    habit: HabitEntity,
    completions: Set<Long>,
    pauses: List<PauseEntity>,
    today: Long,
    month: YearMonth,
    accent: Color,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val offset = month.atDay(1).dayOfWeek.value - 1
    val days = month.lengthOfMonth()
    val rows = (offset + days + 6) / 7
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.IconButton(onClick = onPrev) { Icon(PaxIcons.Prev, "Previous month") }
            Text(
                month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
            androidx.compose.material3.IconButton(onClick = onNext) { Icon(PaxIcons.Next, "Next month") }
        }
        Row {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(
                    it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        for (r in 0 until rows) {
            Row {
                for (c in 0 until 7) {
                    val num = r * 7 + c - offset + 1
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (num in 1..days) {
                            val epoch = month.atDay(num).toEpochDay()
                            DayCell(num, StreakEngine.dayState(habit, completions, pauses, epoch, today), epoch == today, accent)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LegendItem("Completed", accent)
            LegendItem("Missed", MaterialTheme.colorScheme.error)
            LegendItem("Scheduled", accent.copy(alpha = 0.35f))
            LegendItem("Paused", MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DayCell(num: Int, state: DayState, isToday: Boolean, accent: Color) {
    val cs = MaterialTheme.colorScheme
    val bg: Color
    val fg: Color
    var ring: Color? = null
    when (state) {
        DayState.COMPLETED -> { bg = accent; fg = Color.White }
        DayState.MISSED -> { bg = cs.errorContainer; fg = cs.onErrorContainer }
        DayState.SCHEDULED -> { bg = Color.Transparent; fg = cs.onSurface; ring = accent.copy(alpha = 0.55f) }
        DayState.PAUSED -> { bg = cs.surfaceContainerHighest; fg = cs.onSurfaceVariant }
        DayState.NOT_SCHEDULED -> { bg = Color.Transparent; fg = cs.onSurfaceVariant.copy(alpha = 0.55f) }
    }
    val label = "$num, ${state.name.lowercase().replace('_', ' ')}"
    var m: Modifier = Modifier
        .fillMaxSize()
        .clip(CircleShape)
        .background(bg)
        .semantics { contentDescription = label }
    if (ring != null) m = m.border(1.5.dp, ring, CircleShape)
    if (isToday) m = m.border(2.dp, cs.primary, CircleShape)
    Box(m, contentAlignment = Alignment.Center) {
        Text(
            "$num",
            style = MaterialTheme.typography.labelLarge,
            color = fg,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )
    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 4.dp), content = content)
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
    trailing: @Composable (() -> Unit)? = null
) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (trailing != null) trailing()
        }
        if (showDivider) {
            HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
