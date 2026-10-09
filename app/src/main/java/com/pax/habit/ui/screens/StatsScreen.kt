@file:OptIn(ExperimentalMaterial3Api::class)

package com.pax.habit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pax.habit.domain.AppState
import com.pax.habit.domain.StatsEngine
import com.pax.habit.domain.StatsRange
import com.pax.habit.ui.components.BarChart
import com.pax.habit.ui.components.EmptyState
import com.pax.habit.ui.components.Heatmap
import com.pax.habit.ui.components.PaxCard
import com.pax.habit.ui.components.SectionTitle
import com.pax.habit.ui.components.StatTile
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private fun pct(f: Float) = "${(f * 100).roundToInt()}%"

@Composable
fun StatsScreen(state: AppState) {
    var rangeIdx by rememberSaveable { mutableIntStateOf(StatsRange.D30.ordinal) }
    val range = StatsRange.entries[rangeIdx]
    val result = remember(state.habits, state.today, range) { StatsEngine.compute(range, state.habits, state.today) }
    val heat = remember(state.habits, state.today) {
        StatsEngine.dailyFractions(state.habits, state.today - 18 * 7, state.today)
    }
    val primary = MaterialTheme.colorScheme.primary

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Statistics", style = MaterialTheme.typography.headlineLarge)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatsRange.entries.forEach { r ->
                FilterChip(selected = r == range, onClick = { rangeIdx = r.ordinal }, label = { Text(r.label) })
            }
        }

        if (state.habits.isEmpty()) {
            EmptyState("No data yet", "Add a habit and complete it. Your numbers show up here.")
            return@Column
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Overall completion", pct(result.overallRate), Modifier.weight(1f))
            StatTile("Total completions", "${result.totalCompletions}", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Current streak", "${result.currentStreak} days", Modifier.weight(1f), sub = "All habits done")
            StatTile("Best streak", "${result.bestStreak} days", Modifier.weight(1f), sub = "All habits done")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("XP earned", "+${result.xp}", Modifier.weight(1f))
            StatTile("Average completion", pct(result.averageRate), Modifier.weight(1f), sub = "Per day")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                "Most consistent",
                result.most?.first?.name ?: "—",
                Modifier.weight(1f),
                sub = result.most?.let { pct(it.second) }
            )
            StatTile(
                "Least consistent",
                result.least?.first?.name ?: "—",
                Modifier.weight(1f),
                sub = result.least?.let { pct(it.second) }
            )
        }

        PaxCard(Modifier.fillMaxWidth()) {
            SectionTitle(if (result.bars.size == (result.toDay - result.fromDay + 1).toInt()) "Daily completion" else "Completion over time")
            Spacer(Modifier.height(12.dp))
            BarChart(result.bars, primary)
            Spacer(Modifier.height(6.dp))
            val fmt = DateTimeFormatter.ofPattern("MMM d")
            Row {
                Text(
                    LocalDate.ofEpochDay(result.fromDay).format(fmt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    LocalDate.ofEpochDay(result.toDay).format(fmt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        PaxCard(Modifier.fillMaxWidth()) {
            SectionTitle("Activity")
            Spacer(Modifier.height(12.dp))
            Heatmap(heat, state.today, weeks = 18, color = primary)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                listOf(0.18f, 0.35f, 0.55f, 0.78f, 1f).forEach { a ->
                    Box(
                        Modifier
                            .padding(end = 4.dp)
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (a == 0.18f) MaterialTheme.colorScheme.surfaceContainerHighest else primary.copy(alpha = a))
                    )
                }
                Spacer(Modifier.width(2.dp))
                Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        PaxCard(Modifier.fillMaxWidth()) {
            SectionTitle("Insights")
            Spacer(Modifier.height(8.dp))
            insights(result, range).forEach {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun dayName(index: Int): String =
    DayOfWeek.of(index + 1).getDisplayName(TextStyle.FULL, Locale.getDefault())

private fun insights(r: com.pax.habit.domain.StatsResult, range: StatsRange): List<String> = buildList {
    val withData = r.weekday.withIndex().filter { it.value != null }
    if (withData.size >= 2) {
        val best = withData.maxBy { it.value!! }
        val worst = withData.minBy { it.value!! }
        if (best.index != worst.index && best.value!! - worst.value!! >= 0.15f) {
            add("Your strongest day is ${dayName(best.index)} at ${pct(best.value!!)}.")
            add("${dayName(worst.index)} is your toughest at ${pct(worst.value!!)}. A reminder that morning can help.")
        }
    }
    when {
        r.overallRate >= 0.8f -> add("You finished ${pct(r.overallRate)} of what you scheduled in the last ${range.label.lowercase()}. Excellent consistency.")
        r.overallRate >= 0.5f -> add("You're at ${pct(r.overallRate)}. A small push on your weak days will lift it.")
        r.totalCompletions > 0 -> add("Completion is ${pct(r.overallRate)}. Try fewer, simpler habits to rebuild momentum.")
    }
    r.least?.let { if (it.second < 0.5f) add("\"${it.first.name}\" needs attention: ${pct(it.second)} done in this period.") }
    if (isEmpty()) add("Complete a few habits and your insights will show up here.")
}
