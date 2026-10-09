@file:OptIn(ExperimentalMaterial3Api::class)

package com.pax.habit.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.pax.habit.domain.AppState
import com.pax.habit.domain.Achievement
import com.pax.habit.domain.Achievements
import com.pax.habit.domain.Progression
import com.pax.habit.ui.components.PaxCard
import com.pax.habit.ui.components.SectionTitle
import com.pax.habit.ui.components.SettingsRow
import com.pax.habit.ui.components.SettingsSection
import com.pax.habit.ui.hasNotificationPermission
import com.pax.habit.ui.theme.PaxGradient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val CONTACT_EMAIL = "pax.official.com@gmail.com"

@Composable
fun ProfileScreen(
    state: AppState,
    onTheme: (String) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
    onExport: (Uri) -> Unit,
    onImport: (Uri) -> Unit
) {
    val ctx = LocalContext.current
    var themeDialog by remember { mutableStateOf(false) }
    var aboutDialog by remember { mutableStateOf(false) }
    var privacyDialog by remember { mutableStateOf(false) }
    var notifDialog by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<Uri?>(null) }
    var tick by remember { mutableIntStateOf(0) }

    LifecycleResumeEffect(Unit) {
        tick++
        onPauseOrDispose { }
    }
    val notificationsOn = remember(tick) {
        ctx.hasNotificationPermission() && NotificationManagerCompat.from(ctx).areNotificationsEnabled()
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) onExport(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pendingImport = uri
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) openNotificationSettings(ctx)
    }

    val unlocked = state.settings.unlocked
    val intoLevel = Progression.xpIntoLevel(state.totalXp)

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(14.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(PaxGradient)
                .padding(22.dp)
        ) {
            Column {
                Text("Level ${state.level}", style = MaterialTheme.typography.displayMedium, color = Color.White)
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { intoLevel.toFloat() / Progression.XP_PER_LEVEL },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "$intoLevel / ${Progression.XP_PER_LEVEL} XP to level ${state.level + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f)
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("⚡ ${state.totalXp} XP")
                    Pill("✅ ${state.totalCompletions} completed")
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("Achievements")
        Text(
            "${unlocked.size} of ${Achievements.all.size} unlocked",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Achievements.all.chunked(2).forEach { pair ->
            Row(Modifier.padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { a -> AchievementCard(a, a.id in unlocked, Modifier.weight(1f)) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        SettingsSection("Appearance") {
            SettingsRow("Theme", themeLabel(state.settings.theme), onClick = { themeDialog = true })
            SettingsRow(
                "Dynamic colors",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "Use colors from your wallpaper" else "Needs Android 12 or newer",
                showDivider = false
            ) {
                Switch(
                    checked = state.settings.dynamicColor,
                    onCheckedChange = onDynamicColor,
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                )
            }
        }

        SettingsSection("Notifications") {
            SettingsRow(
                "Habit reminders",
                if (notificationsOn) "Allowed. Tap to manage" else "Off. Tap to turn on",
                showDivider = false,
                onClick = { if (notificationsOn) openNotificationSettings(ctx) else notifDialog = true }
            )
        }

        SettingsSection("Your data") {
            SettingsRow(
                "Export backup",
                "Save all habits and history to a file",
                onClick = {
                    exportLauncher.launch("pax_habit_backup_${LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)}.json")
                }
            )
            SettingsRow(
                "Restore backup",
                "Replace current data with a backup file",
                showDivider = false,
                onClick = { importLauncher.launch(arrayOf("*/*")) }
            )
        }

        SettingsSection("About") {
            SettingsRow("About Pax Habit", onClick = { aboutDialog = true })
            SettingsRow("Privacy policy", onClick = { privacyDialog = true })
            SettingsRow("Contact us", CONTACT_EMAIL, onClick = { sendEmail(ctx) })
            SettingsRow("Version", appVersion(ctx), showDivider = false)
        }
        Spacer(Modifier.height(24.dp))
    }

    if (themeDialog) {
        AlertDialog(
            onDismissRequest = { themeDialog = false },
            title = { Text("Theme") },
            text = {
                Column {
                    listOf("system" to "System default", "light" to "Light", "dark" to "Dark").forEach { (value, label) ->
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.settings.theme == value,
                                onClick = {
                                    onTheme(value)
                                    themeDialog = false
                                }
                            )
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { themeDialog = false }) { Text("Close") } }
        )
    }

    if (notifDialog) {
        AlertDialog(
            onDismissRequest = { notifDialog = false },
            title = { Text("Turn on reminders?") },
            text = {
                Text("Pax Habit uses notifications only to remind you about habits at the time you choose. Nothing else.")
            },
            confirmButton = {
                TextButton(onClick = {
                    notifDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !ctx.hasNotificationPermission()) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        openNotificationSettings(ctx)
                    }
                }) { Text("Continue") }
            },
            dismissButton = { TextButton(onClick = { notifDialog = false }) { Text("Not now") } }
        )
    }

    pendingImport?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Restore this backup?") },
            text = { Text("This replaces all habits, history and progress on this phone with the contents of the file.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingImport = null
                    onImport(uri)
                }) { Text("Restore") }
            },
            dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Cancel") } }
        )
    }

    if (aboutDialog) {
        AlertDialog(
            onDismissRequest = { aboutDialog = false },
            title = { Text("Pax Habit") },
            text = {
                Text("Build habits that stick. Track streaks, earn XP and see your progress. Everything works offline and stays on your phone.\n\nMade by Pax.")
            },
            confirmButton = { TextButton(onClick = { aboutDialog = false }) { Text("Close") } }
        )
    }

    if (privacyDialog) {
        AlertDialog(
            onDismissRequest = { privacyDialog = false },
            title = { Text("Privacy policy") },
            text = {
                Text(
                    "Pax Habit has no account, no login and no cloud sync. Your habits, history and progress are stored only on this device.\n\n" +
                        "The app collects no personal data and sends nothing over the internet. A backup file leaves your phone only if you save or share it yourself.\n\n" +
                        "Questions: $CONTACT_EMAIL"
                )
            },
            confirmButton = { TextButton(onClick = { privacyDialog = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun Pill(text: String) {
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
private fun AchievementCard(a: Achievement, unlocked: Boolean, modifier: Modifier = Modifier) {
    PaxCard(
        modifier.alpha(if (unlocked) 1f else 0.5f),
        color = if (unlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
    ) {
        Text(if (unlocked) a.emoji else "🔒", fontSize = 28.sp)
        Spacer(Modifier.height(6.dp))
        Text(a.title, style = MaterialTheme.typography.titleSmall)
        Text(
            a.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun themeLabel(value: String) = when (value) {
    "light" -> "Light"
    "dark" -> "Dark"
    else -> "System default"
}

private fun appVersion(ctx: Context): String =
    try {
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: ""
    } catch (e: Exception) {
        ""
    }

private fun openNotificationSettings(ctx: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        ctx.startActivity(intent)
    } catch (e: Exception) {
        // No settings screen available. Nothing else to do.
    }
}

private fun sendEmail(ctx: Context) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$CONTACT_EMAIL"))
        .putExtra(Intent.EXTRA_SUBJECT, "Pax Habit feedback")
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        ctx.startActivity(intent)
    } catch (e: Exception) {
        // No email app installed.
    }
}
