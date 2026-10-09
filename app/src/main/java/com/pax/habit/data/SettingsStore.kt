package com.pax.habit.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pax_settings")

class SettingsStore(private val context: Context) {
    private val theme = stringPreferencesKey("theme")
    private val dynamic = booleanPreferencesKey("dynamic_color")
    private val xpBanked = intPreferencesKey("xp_banked")
    private val unlocked = stringSetPreferencesKey("unlocked_achievements")

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            theme = p[theme] ?: "system",
            dynamicColor = p[dynamic] ?: false,
            xpBanked = p[xpBanked] ?: 0,
            unlocked = p[unlocked] ?: emptySet()
        )
    }

    suspend fun setTheme(value: String) {
        context.dataStore.edit { it[theme] = value }
    }

    suspend fun setDynamicColor(value: Boolean) {
        context.dataStore.edit { it[dynamic] = value }
    }

    suspend fun addXpBanked(amount: Int) {
        context.dataStore.edit { it[xpBanked] = (it[xpBanked] ?: 0) + amount }
    }

    suspend fun addUnlocked(ids: Set<String>) {
        context.dataStore.edit { it[unlocked] = (it[unlocked] ?: emptySet()) + ids }
    }

    suspend fun replaceProgress(xp: Int, ids: Set<String>) {
        context.dataStore.edit {
            it[xpBanked] = xp
            it[unlocked] = ids
        }
    }
}
