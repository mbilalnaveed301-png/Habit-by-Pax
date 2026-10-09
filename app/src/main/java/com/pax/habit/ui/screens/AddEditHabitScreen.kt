@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.pax.habit.ui.screens

import android.Manifest
import android.os.Build
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pax.habit.data.HabitEntity
import com.pax.habit.data.SCHEDULE_DAILY
import com.pax.habit.data.SCHEDULE_INTERVAL
import com.pax.habit.data.SCHEDULE_WEEKDAYS
import com.pax.habit.domain.Schedule
import com.pax.habit.ui.components.HabitColors
import com.pax.habit.ui.components.HabitIconOptions
import com.pax.habit.ui.components.PaxIcons
import com.pax.habit.ui.formatMinutes
import com.pax.habit.ui.hasNotificationPermission
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AddEditHabitScreen(
    initial: HabitEntity?,
    today: Long,
    onSave: (HabitEntity) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "") }
    var description by rememberSaveable { mutableStateOf(initial?.description ?: "") }
    var iconKey by rememberSaveable { mutableStateOf(initial?.iconKey ?: "study") }
    var colorArgb by rememberSaveable { mutableIntStateOf(initial?.colorArgb ?: HabitColors[0]) }
    var scheduleType by rememberSaveable { mutableIntStateOf(initial?.scheduleType ?: SCHEDULE_DAILY) }
    var daysMask by rememberSaveable { mutableIntStateOf(initial?.daysMask ?: Schedule.WEEKDAYS) }
    var interval by rememberSaveable { mutableIntStateOf((initial?.intervalDays ?: 2).coerceAtLeast(2)) }
    var reminder by rememberSaveable { mutableStateOf(initial?.reminderMinutes) }
    var startDay by rememberSaveable { mutableLongStateOf(initial?.startDay ?: today) }
    var goalOn by rememberSaveable { mutableStateOf(initial?.goalValue != null) }
    var goalText by rememberSaveable { mutableStateOf(initial?.goalValue?.toString() ?: "") }
    var goalUnit by rememberSaveable { mutableStateOf(initial?.goalUnit ?: "") }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var showTime by rememberSaveable { mutableStateOf(false) }
    var showDate by rememberSaveable { mutableStateOf(false) }
    var showRationale by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    val nameOk = name.trim().isNotEmpty()
    val daysOk = scheduleType != SCHEDULE_WEEKDAYS || (daysMask and Schedule.ALL_DAYS) != 0
    val goalAmount = goalText.toIntOrNull()
    val goalOk = !goalOn || (goalAmount != null && goalAmount in 1..100000)
    val color = Color(colorArgb)

    fun save() {
        showErrors = true
        if (!(nameOk && daysOk && goalOk)) return
        onSave(
            HabitEntity(
                id = initial?.id ?: 0L,
                name = name.trim().replace(Regex("\\s+"), " "),
                description = description.trim(),
                iconKey = iconKey,
                colorArgb = colorArgb,
                scheduleType = scheduleType,
                daysMask = if (scheduleType == SCHEDULE_WEEKDAYS) daysMask and Schedule.ALL_DAYS else Schedule.ALL_DAYS,
                intervalDays = if (scheduleType == SCHEDULE_INTERVAL) interval else 1,
                startDay = startDay,
                reminderMinutes = reminder,
                goalValue = if (goalOn) goalAmount else null,
                goalUnit = if (goalOn) goalUnit.trim() else "",
                createdAt = initial?.createdAt ?: System.currentTimeMillis()
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) "New habit" else "Edit habit") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(PaxIcons.Close, contentDescription = "Close without saving") }
                },
                actions = {
                    Button(onClick = { save() }, modifier = Modifier.padding(end = 12.dp)) { Text("Save") }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Field("Name") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showErrors && !nameOk,
                    placeholder = { Text("e.g. Study") },
                    supportingText = { Text(if (showErrors && !nameOk) "Give your habit a name" else "${name.length}/40") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Field("Description (optional)") {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.take(120) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text("e.g. Read before sleeping.") },
                    supportingText = { Text("${description.length}/120") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Field("Icon") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HabitIconOptions.forEach { option ->
                        val selected = option.key == iconKey
                        Column(
                            Modifier
                                .width(62.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { iconKey = option.key }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .then(if (selected) Modifier.border(2.dp, color, RoundedCornerShape(16.dp)) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    option.icon,
                                    contentDescription = option.label + if (selected) ", selected" else "",
                                    tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(option.label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }

            Field("Color") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HabitColors.forEach { argb ->
                        val selected = argb == colorArgb
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(argb))
                                .clickable { colorArgb = argb }
                                .semantics { contentDescription = if (selected) "Color, selected" else "Color" },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) Icon(PaxIcons.Check, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            Field("Repeat") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        SCHEDULE_DAILY to "Every day",
                        SCHEDULE_WEEKDAYS to "Specific days",
                        SCHEDULE_INTERVAL to "Custom"
                    ).forEach { (type, label) ->
                        FilterChip(selected = scheduleType == type, onClick = { scheduleType = type }, label = { Text(label) })
                    }
                }
                if (scheduleType == SCHEDULE_WEEKDAYS) {
                    Spacer(Modifier.height(12.dp))
                    DayToggles(daysMask) { daysMask = it }
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = { daysMask = Schedule.WEEKDAYS }, label = { Text("Weekdays") })
                        AssistChip(onClick = { daysMask = Schedule.WEEKEND }, label = { Text("Weekends") })
                        AssistChip(onClick = { daysMask = 0b0010101 }, label = { Text("Mon · Wed · Fri") })
                        AssistChip(onClick = { daysMask = 0b0001010 }, label = { Text("Tue · Thu") })
                    }
                    if (showErrors && !daysOk) {
                        Text(
                            "Pick at least one day",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    } else {
                        Text(
                            Schedule.maskLabel(daysMask),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
                if (scheduleType == SCHEDULE_INTERVAL) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(onClick = { interval = (interval - 1).coerceAtLeast(2) }) {
                            Icon(PaxIcons.Minus, contentDescription = "Fewer days")
                        }
                        Text(
                            "Every $interval days",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        FilledTonalIconButton(onClick = { interval = (interval + 1).coerceAtMost(60) }) {
                            Icon(PaxIcons.Add, contentDescription = "More days")
                        }
                    }
                }
            }

            Field("Reminder") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (reminder == null) "No reminder" else "Remind me daily",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = reminder != null,
                        onCheckedChange = { on ->
                            if (on) {
                                reminder = reminder ?: (9 * 60)
                                if (!ctx.hasNotificationPermission()) showRationale = true
                            } else {
                                reminder = null
                            }
                        }
                    )
                }
                reminder?.let { minutes ->
                    OutlinedButton(onClick = { showTime = true }, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(PaxIcons.Clock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(formatMinutes(minutes))
                    }
                }
            }

            Field("Start date") {
                OutlinedButton(onClick = { showDate = true }) {
                    Icon(PaxIcons.Calendar, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(LocalDate.ofEpochDay(startDay).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
                }
                Text(
                    "The habit never shows as missed before this day.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Field("Goal (optional)") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Track a measurable goal", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = goalOn, onCheckedChange = { goalOn = it })
                }
                if (goalOn) {
                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = goalText,
                            onValueChange = { goalText = it.filter(Char::isDigit).take(6) },
                            modifier = Modifier.weight(0.4f),
                            singleLine = true,
                            label = { Text("Amount") },
                            isError = showErrors && !goalOk,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp)
                        )
                        OutlinedTextField(
                            value = goalUnit,
                            onValueChange = { goalUnit = it.take(20) },
                            modifier = Modifier.weight(0.6f),
                            singleLine = true,
                            label = { Text("Unit") },
                            placeholder = { Text("minutes") },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                    if (showErrors && !goalOk) {
                        Text(
                            "Enter a number greater than 0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showTime) {
        TimeDialog(
            initialMinutes = reminder ?: (9 * 60),
            onDismiss = { showTime = false },
            onConfirm = {
                reminder = it
                showTime = false
            }
        )
    }

    if (showDate) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = startDay * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { startDay = Math.floorDiv(it, 86_400_000L) }
                    showDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel") } }
        ) { DatePicker(state = pickerState) }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Turn on reminders?") },
            text = {
                Text(
                    "Pax Habit sends a notification at the time you choose so you don't forget a habit. " +
                        "It sends nothing else, and you can change this any time in Profile."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) { Text("Allow") }
            },
            dismissButton = { TextButton(onClick = { showRationale = false }) { Text("Not now") } }
        )
    }
}

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun DayToggles(mask: Int, onChange: (Int) -> Unit) {
    val letters = listOf("M", "T", "W", "T", "F", "S", "S")
    val names = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        letters.forEachIndexed { i, letter ->
            val on = ((mask shr i) and 1) == 1
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (on) cs.primary else Color.Transparent)
                    .border(1.dp, if (on) cs.primary else cs.outline, CircleShape)
                    .toggleable(value = on, role = Role.Checkbox, onValueChange = { onChange(mask xor (1 shl i)) })
                    .semantics { contentDescription = names[i] },
                contentAlignment = Alignment.Center
            ) {
                Text(letter, color = if (on) cs.onPrimary else cs.onSurface, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun TimeDialog(initialMinutes: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val ctx = LocalContext.current
    val narrow = LocalConfiguration.current.screenWidthDp < 400
    val state = rememberTimePickerState(
        initialHour = initialMinutes / 60,
        initialMinute = initialMinutes % 60,
        is24Hour = DateFormat.is24HourFormat(ctx)
    )
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(28.dp), tonalElevation = 6.dp, modifier = Modifier.padding(8.dp)) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Reminder time",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
                if (narrow) TimeInput(state = state) else TimePicker(state = state)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) { Text("OK") }
                }
            }
        }
    }
}
