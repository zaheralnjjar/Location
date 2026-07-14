package com.directnumber.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/** Single DataStore instance for all local settings — no cloud sync, no external server. */
val Context.settingsDataStore by preferencesDataStore(name = "direct_number_settings")
