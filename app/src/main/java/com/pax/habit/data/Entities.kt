package com.pax.habit.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

const val SCHEDULE_DAILY = 0
const val SCHEDULE_WEEKDAYS = 1
const val SCHEDULE_INTERVAL = 2

/**
 * A habit. The schedule is stored structurally (type + weekday bitmask + interval),
 * never as free text, so streaks can always be recomputed from it.
 *
 * daysMask: bit 0 = Monday ... bit 6 = Sunday.
 * All dates are epoch days (LocalDate.toEpochDay()).
 */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconKey: String = "study",
    val colorArgb: Int,
    val scheduleType: Int = SCHEDULE_DAILY,
    val daysMask: Int = 127,
    val intervalDays: Int = 1,
    val startDay: Long,
    val reminderMinutes: Int? = null,
    val goalValue: Int? = null,
    val goalUnit: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "completions",
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["habitId", "epochDay"], unique = true)]
)
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val epochDay: Long,
    val completedAt: Long,
    val xp: Int,
    /** Reserved for measurable goals (e.g. minutes actually done). */
    val amount: Int? = null
)

/** A pause period. endDay == null means the habit is paused right now. */
@Entity(
    tableName = "pauses",
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["habitId"])]
)
data class PauseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val startDay: Long,
    val endDay: Long? = null
)
