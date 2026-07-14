package com.directnumber.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.directnumber.app.data.model.AppLanguageOption
import com.directnumber.app.data.model.AppSettings
import com.directnumber.app.data.model.ThemeMode
import com.directnumber.app.data.model.WhatsAppTarget
import com.directnumber.app.data.preferences.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private object Keys {
    val DEFAULT_REGION = stringPreferencesKey("default_region_code")
    val LAST_REGION = stringPreferencesKey("last_used_region_code")
    val REMEMBER_LAST_COUNTRY = booleanPreferencesKey("remember_last_country")
    val SHOW_RECENT_COUNTRIES = booleanPreferencesKey("show_recent_countries")
    val WHATSAPP_TARGET = stringPreferencesKey("whatsapp_target")
    val LANGUAGE = stringPreferencesKey("language")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val KEEP_RECENT_NUMBERS = booleanPreferencesKey("keep_recent_numbers_history")
}

/** Reads/writes [AppSettings] to Jetpack DataStore. All state lives on-device only. */
class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            defaultRegionCode = prefs[Keys.DEFAULT_REGION] ?: "AR",
            lastUsedRegionCode = prefs[Keys.LAST_REGION],
            rememberLastCountry = prefs[Keys.REMEMBER_LAST_COUNTRY] ?: true,
            showRecentCountries = prefs[Keys.SHOW_RECENT_COUNTRIES] ?: true,
            whatsAppTarget = prefs[Keys.WHATSAPP_TARGET]?.let { runCatching { WhatsAppTarget.valueOf(it) }.getOrNull() }
                ?: WhatsAppTarget.ASK_EVERY_TIME,
            language = prefs[Keys.LANGUAGE]?.let { runCatching { AppLanguageOption.valueOf(it) }.getOrNull() }
                ?: AppLanguageOption.SYSTEM,
            themeMode = prefs[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            keepRecentNumbersHistory = prefs[Keys.KEEP_RECENT_NUMBERS] ?: false,
        )
    }

    suspend fun setDefaultRegion(regionCode: String) {
        context.settingsDataStore.edit { it[Keys.DEFAULT_REGION] = regionCode }
    }

    suspend fun setLastUsedRegion(regionCode: String) {
        context.settingsDataStore.edit { it[Keys.LAST_REGION] = regionCode }
    }

    suspend fun setRememberLastCountry(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.REMEMBER_LAST_COUNTRY] = enabled }
    }

    suspend fun setShowRecentCountries(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.SHOW_RECENT_COUNTRIES] = enabled }
    }

    suspend fun setWhatsAppTarget(target: WhatsAppTarget) {
        context.settingsDataStore.edit { it[Keys.WHATSAPP_TARGET] = target.name }
    }

    suspend fun setLanguage(language: AppLanguageOption) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setKeepRecentNumbersHistory(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.KEEP_RECENT_NUMBERS] = enabled }
    }
}
