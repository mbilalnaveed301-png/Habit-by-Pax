# Pax Habit

Offline-first habit tracker for Android. Kotlin, Jetpack Compose, Material 3, Room, DataStore.

## Build the APK with GitHub Actions
1. Create a new GitHub repository and push this whole folder to the `main` branch.
2. Open the **Actions** tab, pick **Build debug APK** (it also runs on every push).
3. When the run finishes, download the **PaxHabit-debug-apk** artifact, unzip it and install `app-debug.apk`.

No Gradle wrapper is committed: the workflow installs Gradle 8.10.2 itself.
To build locally, use Android Studio (Ladybug or newer) and open this folder.

## Structure
- `data/`      Room entities, DAO, repository, DataStore settings
- `domain/`    schedule rules, streak engine, stats, XP and achievements (pure Kotlin)
- `notify/`    reminder alarms, notification receiver, reboot receiver
- `backup/`    JSON export and restore
- `ui/`        theme, components, screens, navigation, view model

## Streak rules
Streaks are recomputed from real completion dates every time, never incremented.
Days that are not scheduled, paused or before the start date neither count nor break a streak.
A missed scheduled day in the past breaks it. Today never breaks a streak until the day is over.
