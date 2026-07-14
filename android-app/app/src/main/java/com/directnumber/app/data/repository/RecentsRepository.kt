package com.directnumber.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.directnumber.app.data.preferences.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class RecentNumberEntry(val e164: String, val name: String)

private object RecentKeys {
    val RECENT_REGIONS = stringPreferencesKey("recent_region_codes")
    val RECENT_NUMBERS = stringPreferencesKey("recent_numbers_history")
}

private const val MAX_RECENT_REGIONS = 5
private const val MAX_RECENT_NUMBERS = 20
private const val REGION_SEPARATOR = ","
private const val NUMBER_ENTRY_SEPARATOR = "\n"
private const val NUMBER_FIELD_SEPARATOR = "::"

/**
 * Recently used countries are a convenience feature and always allowed.
 * Recent *numbers*, on the other hand, are personal data: entries are only ever written
 * when the user explicitly enabled "keep recent numbers history" in Settings (off by default),
 * and the whole list can be wiped with [clearRecentNumbers].
 */
class RecentsRepository(private val context: Context) {

    val recentRegionCodes: Flow<List<String>> = context.settingsDataStore.data.map { prefs ->
        prefs[RecentKeys.RECENT_REGIONS]?.split(REGION_SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun pushRecentRegion(regionCode: String) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[RecentKeys.RECENT_REGIONS]?.split(REGION_SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()
            val updated = (listOf(regionCode) + current.filterNot { it == regionCode }).take(MAX_RECENT_REGIONS)
            prefs[RecentKeys.RECENT_REGIONS] = updated.joinToString(REGION_SEPARATOR)
        }
    }

    val recentNumbers: Flow<List<RecentNumberEntry>> = context.settingsDataStore.data.map { prefs ->
        prefs[RecentKeys.RECENT_NUMBERS]?.split(NUMBER_ENTRY_SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { line ->
                val parts = line.split(NUMBER_FIELD_SEPARATOR, limit = 2)
                if (parts.size == 2) RecentNumberEntry(e164 = parts[0], name = parts[1]) else null
            } ?: emptyList()
    }

    suspend fun pushRecentNumber(entry: RecentNumberEntry, historyEnabled: Boolean) {
        if (!historyEnabled) return
        context.settingsDataStore.edit { prefs ->
            val current = prefs[RecentKeys.RECENT_NUMBERS]?.split(NUMBER_ENTRY_SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()
            val newLine = "${entry.e164}$NUMBER_FIELD_SEPARATOR${entry.name}"
            val updated = (listOf(newLine) + current.filterNot { it.startsWith("${entry.e164}$NUMBER_FIELD_SEPARATOR") })
                .take(MAX_RECENT_NUMBERS)
            prefs[RecentKeys.RECENT_NUMBERS] = updated.joinToString(NUMBER_ENTRY_SEPARATOR)
        }
    }

    suspend fun clearRecentNumbers() {
        context.settingsDataStore.edit { prefs -> prefs.remove(RecentKeys.RECENT_NUMBERS) }
    }
}
