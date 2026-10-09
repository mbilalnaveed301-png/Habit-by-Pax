package com.pax.habit.domain

object Progression {
    const val XP_PER_LEVEL = 200
    const val BASE_XP = 10

    fun level(totalXp: Int): Int = totalXp / XP_PER_LEVEL + 1
    fun xpIntoLevel(totalXp: Int): Int = totalXp % XP_PER_LEVEL

    /** 10 XP per completion, +25 every time a habit hits a multiple of 7 days in a row. */
    fun xpForCompletion(newStreak: Int): Int =
        BASE_XP + if (newStreak > 0 && newStreak % 7 == 0) 25 else 0
}

data class Achievement(val id: String, val emoji: String, val title: String, val description: String)

data class ProgressSnapshot(
    val totalCompletions: Int,
    val bestHabitStreak: Int,
    val habitCount: Int,
    val level: Int,
    val perfectDay: Boolean
)

object Achievements {
    val all = listOf(
        Achievement("first_step", "🌱", "First step", "Complete your first habit"),
        Achievement("ten", "🔥", "Warming up", "Complete 10 habits"),
        Achievement("fifty", "💪", "Committed", "Complete 50 habits"),
        Achievement("hundred", "💯", "Century", "Complete 100 habits"),
        Achievement("streak7", "📅", "One week strong", "Reach a 7 day streak"),
        Achievement("streak30", "🏅", "Monthly master", "Reach a 30 day streak"),
        Achievement("streak100", "🚀", "Unstoppable", "Reach a 100 day streak"),
        Achievement("builder", "🧱", "Habit builder", "Create 5 habits"),
        Achievement("perfect", "✨", "Perfect day", "Finish 3 or more habits in one day"),
        Achievement("level5", "⭐", "Rising star", "Reach level 5")
    )

    fun byId(id: String): Achievement? = all.firstOrNull { it.id == id }

    fun evaluate(s: ProgressSnapshot): Set<String> = buildSet {
        if (s.totalCompletions >= 1) add("first_step")
        if (s.totalCompletions >= 10) add("ten")
        if (s.totalCompletions >= 50) add("fifty")
        if (s.totalCompletions >= 100) add("hundred")
        if (s.bestHabitStreak >= 7) add("streak7")
        if (s.bestHabitStreak >= 30) add("streak30")
        if (s.bestHabitStreak >= 100) add("streak100")
        if (s.habitCount >= 5) add("builder")
        if (s.perfectDay) add("perfect")
        if (s.level >= 5) add("level5")
    }
}
