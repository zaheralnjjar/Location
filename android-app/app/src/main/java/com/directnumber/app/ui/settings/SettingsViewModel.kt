package com.directnumber.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.directnumber.app.DirectNumberApp
import com.directnumber.app.R
import com.directnumber.app.data.model.AppLanguageOption
import com.directnumber.app.data.model.AppSettings
import com.directnumber.app.data.model.ThemeMode
import com.directnumber.app.data.model.WhatsAppTarget
import com.directnumber.app.data.repository.CountryRepository
import com.directnumber.app.data.repository.RecentsRepository
import com.directnumber.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val recentsRepository: RecentsRepository,
    private val countryRepository: CountryRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    private val _snackbarEvents = MutableSharedFlow<Int>()
    val snackbarEvents: SharedFlow<Int> = _snackbarEvents.asSharedFlow()

    fun countriesForPicker(query: String, locale: Locale) = countryRepository.search(query, locale)

    fun setDefaultRegion(regionCode: String) = viewModelScope.launch {
        settingsRepository.setDefaultRegion(regionCode)
    }

    fun setWhatsAppTarget(target: WhatsAppTarget) = viewModelScope.launch {
        settingsRepository.setWhatsAppTarget(target)
    }

    fun setLanguage(language: AppLanguageOption) = viewModelScope.launch {
        settingsRepository.setLanguage(language)
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        settingsRepository.setThemeMode(mode)
    }

    fun setRememberLastCountry(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setRememberLastCountry(enabled)
    }

    fun setShowRecentCountries(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setShowRecentCountries(enabled)
    }

    fun setKeepRecentNumbersHistory(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setKeepRecentNumbersHistory(enabled)
        if (!enabled) recentsRepository.clearRecentNumbers()
    }

    fun clearRecentNumbersHistory() = viewModelScope.launch {
        recentsRepository.clearRecentNumbers()
        _snackbarEvents.emit(R.string.settings_clear_history_done)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DirectNumberApp
                SettingsViewModel(
                    settingsRepository = app.settingsRepository,
                    recentsRepository = app.recentsRepository,
                    countryRepository = app.countryRepository,
                )
            }
        }
    }
}
