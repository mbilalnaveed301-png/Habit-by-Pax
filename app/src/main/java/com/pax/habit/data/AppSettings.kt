package com.pax.habit.data

data class AppSettings(
    val theme: String = "system", // system | light | dark
    val dynamicColor: Boolean = false,
    /** XP carried over from habits the user deleted, so levels never drop. */
    val xpBanked: Int = 0,
    val unlocked: Set<String> = emptySet()
)
