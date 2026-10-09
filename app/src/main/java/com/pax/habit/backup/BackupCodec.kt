package com.pax.habit.backup

import com.pax.habit.data.CompletionEntity
import com.pax.habit.data.HabitEntity
import com.pax.habit.data.PauseEntity
import org.json.JSONArray
import org.json.JSONObject

data class BackupData(
    val habits: List<HabitEntity>,
    val completions: List<CompletionEntity>,
    val pauses: List<PauseEntity>,
    val xpBanked: Int,
    val unlocked: Set<String>
)

/** Plain JSON, human readable, versioned. Nothing leaves the device unless the user saves the file somewhere. */
object BackupCodec {
    const val VERSION = 1

    fun encode(d: BackupData): String {
        val root = JSONObject()
        root.put("app", "PaxHabit")
        root.put("version", VERSION)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("xpBanked", d.xpBanked)
        root.put("unlocked", JSONArray(d.unlocked.toList()))
        root.put("habits", JSONArray().also { arr ->
            d.habits.forEach { h ->
                arr.put(JSONObject().apply {
                    put("id", h.id)
                    put("name", h.name)
                    put("description", h.description)
                    put("iconKey", h.iconKey)
                    put("colorArgb", h.colorArgb)
                    put("scheduleType", h.scheduleType)
                    put("daysMask", h.daysMask)
                    put("intervalDays", h.intervalDays)
                    put("startDay", h.startDay)
                    put("reminderMinutes", h.reminderMinutes ?: JSONObject.NULL)
                    put("goalValue", h.goalValue ?: JSONObject.NULL)
                    put("goalUnit", h.goalUnit)
                    put("createdAt", h.createdAt)
                })
            }
        })
        root.put("completions", JSONArray().also { arr ->
            d.completions.forEach { c ->
                arr.put(JSONObject().apply {
                    put("id", c.id)
                    put("habitId", c.habitId)
                    put("epochDay", c.epochDay)
                    put("completedAt", c.completedAt)
                    put("xp", c.xp)
                    put("amount", c.amount ?: JSONObject.NULL)
                })
            }
        })
        root.put("pauses", JSONArray().also { arr ->
            d.pauses.forEach { p ->
                arr.put(JSONObject().apply {
                    put("id", p.id)
                    put("habitId", p.habitId)
                    put("startDay", p.startDay)
                    put("endDay", p.endDay ?: JSONObject.NULL)
                })
            }
        })
        return root.toString(2)
    }

    /** Throws if the file is not a valid Pax Habit backup. */
    fun decode(json: String): BackupData {
        val root = JSONObject(json)
        require(root.optString("app") == "PaxHabit") { "Not a Pax Habit backup" }
        require(root.optInt("version", 0) in 1..VERSION) { "Unsupported backup version" }

        val habits = root.getJSONArray("habits").objects().map { o ->
            HabitEntity(
                id = o.getLong("id"),
                name = o.getString("name").trim().also { require(it.isNotEmpty()) { "Habit without a name" } },
                description = o.optString("description", ""),
                iconKey = o.optString("iconKey", "study"),
                colorArgb = o.getInt("colorArgb"),
                scheduleType = o.getInt("scheduleType"),
                daysMask = o.getInt("daysMask"),
                intervalDays = o.getInt("intervalDays").coerceAtLeast(1),
                startDay = o.getLong("startDay"),
                reminderMinutes = if (o.isNull("reminderMinutes")) null else o.getInt("reminderMinutes"),
                goalValue = if (o.isNull("goalValue")) null else o.getInt("goalValue"),
                goalUnit = o.optString("goalUnit", ""),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
        val ids = habits.map { it.id }.toSet()
        val completions = root.getJSONArray("completions").objects().map { o ->
            CompletionEntity(
                id = o.getLong("id"),
                habitId = o.getLong("habitId"),
                epochDay = o.getLong("epochDay"),
                completedAt = o.getLong("completedAt"),
                xp = o.getInt("xp"),
                amount = if (o.isNull("amount")) null else o.getInt("amount")
            )
        }.filter { it.habitId in ids }.distinctBy { it.habitId to it.epochDay }
        val pauses = root.getJSONArray("pauses").objects().map { o ->
            PauseEntity(
                id = o.getLong("id"),
                habitId = o.getLong("habitId"),
                startDay = o.getLong("startDay"),
                endDay = if (o.isNull("endDay")) null else o.getLong("endDay")
            )
        }.filter { it.habitId in ids }

        val unlocked = root.optJSONArray("unlocked")?.let { arr -> (0 until arr.length()).map { arr.getString(it) }.toSet() }
            ?: emptySet()
        return BackupData(habits, completions, pauses, root.optInt("xpBanked", 0), unlocked)
    }

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
}
