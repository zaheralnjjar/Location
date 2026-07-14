package com.directnumber.app.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.directnumber.app.DirectNumberApp
import com.directnumber.app.R
import com.directnumber.app.data.model.WhatsAppTarget
import com.directnumber.app.data.repository.CountryRepository
import com.directnumber.app.data.repository.RecentNumberEntry
import com.directnumber.app.data.repository.RecentsRepository
import com.directnumber.app.data.repository.SettingsRepository
import com.directnumber.app.domain.usecase.BuildWhatsAppLinkUseCase
import com.directnumber.app.domain.usecase.GenerateVCardUseCase
import com.directnumber.app.domain.usecase.ValidatePhoneNumberUseCase
import com.directnumber.app.util.ClipboardHelper
import com.directnumber.app.util.ContactLaunchResult
import com.directnumber.app.util.ContactLauncher
import com.directnumber.app.util.PhoneNumberFormatter
import com.directnumber.app.util.VCardGenerator
import com.directnumber.app.util.WhatsAppLaunchResult
import com.directnumber.app.util.WhatsAppLauncher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

enum class CopyType { INTERNATIONAL, WHATSAPP, E164 }

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val recentsRepository: RecentsRepository,
    private val countryRepository: CountryRepository,
    private val validatePhoneNumberUseCase: ValidatePhoneNumberUseCase,
    private val buildWhatsAppLinkUseCase: BuildWhatsAppLinkUseCase,
    private val generateVCardUseCase: GenerateVCardUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<HomeSnackbarEvent>()
    val snackbarEvents: SharedFlow<HomeSnackbarEvent> = _snackbarEvents.asSharedFlow()

    private var keepRecentNumbersHistory = false

    init {
        val quickPickCountries = countryRepository.quickPickRegionCodes.mapNotNull(countryRepository::getCountry)
        viewModelScope.launch {
            combine(settingsRepository.settings, recentsRepository.recentRegionCodes) { settings, recents -> settings to recents }
                .collect { (settings, recents) ->
                    keepRecentNumbersHistory = settings.keepRecentNumbersHistory
                    _uiState.update { state ->
                        val regionCode = if (state.isReady) state.selectedRegionCode else settings.startingRegionCode()
                        state.copy(
                            selectedRegionCode = regionCode,
                            selectedCountry = countryRepository.getCountry(regionCode),
                            quickPickCountries = quickPickCountries,
                            recentRegionCodes = recents,
                            showRecentCountries = settings.showRecentCountries,
                            whatsAppTarget = settings.whatsAppTarget,
                            isReady = true,
                        )
                    }
                }
        }
    }

    fun onContactNameChange(value: String) {
        _uiState.update { it.copy(contactName = value) }
    }

    fun onPhoneInputChange(rawValue: String) {
        val sanitized = PhoneNumberFormatter.sanitizeInput(rawValue)
        _uiState.update { it.copy(rawPhoneInput = sanitized) }
        revalidate()
    }

    fun onClearPhoneInput() {
        _uiState.update { it.copy(rawPhoneInput = "", phoneResult = null) }
    }

    fun onPasteRequested(context: Context) {
        val pasted = ClipboardHelper.paste(context) ?: return
        onPhoneInputChange(pasted)
    }

    fun onPreparedMessageChange(value: String) {
        _uiState.update { it.copy(preparedMessage = value) }
    }

    fun onCountrySelected(regionCode: String) {
        val country = countryRepository.getCountry(regionCode)
        _uiState.update { it.copy(selectedRegionCode = regionCode, selectedCountry = country, activeSheet = null) }
        revalidate()
        viewModelScope.launch {
            settingsRepository.setLastUsedRegion(regionCode)
            recentsRepository.pushRecentRegion(regionCode)
        }
    }

    fun onAcceptDetectedRegion() {
        val detected = _uiState.value.phoneResult?.detectedRegionCode ?: return
        onCountrySelected(detected)
    }

    fun onKeepSelectedRegion() {
        revalidate(forceSelectedRegion = true)
    }

    private fun revalidate(forceSelectedRegion: Boolean = false) {
        _uiState.update { state ->
            val result = if (state.rawPhoneInput.isBlank()) {
                null
            } else {
                validatePhoneNumberUseCase(state.rawPhoneInput, state.selectedRegionCode, forceSelectedRegion)
            }
            state.copy(phoneResult = result)
        }
    }

    fun onOpenCountryPicker() = _uiState.update { it.copy(activeSheet = HomeSheet.CountryPicker) }
    fun onOpenCopySheet() = _uiState.update { it.copy(activeSheet = HomeSheet.CopyActions) }
    fun onDismissSheet() = _uiState.update { it.copy(activeSheet = null) }
    fun onDismissDialog() = _uiState.update { it.copy(activeDialog = null) }

    fun countriesForPicker(query: String, locale: Locale) = countryRepository.search(query, locale)

    fun onOpenWhatsAppClicked(context: Context, explicitTarget: WhatsAppTarget? = null) {
        val result = _uiState.value.phoneResult ?: return
        val whatsAppNumber = result.whatsAppNumber?.takeIf { result.isActionable } ?: return
        val message = _uiState.value.preparedMessage.ifBlank { null }
        val target = explicitTarget ?: _uiState.value.whatsAppTarget

        if (target == WhatsAppTarget.ASK_EVERY_TIME) {
            _uiState.update { it.copy(activeSheet = HomeSheet.WhatsAppPicker(whatsAppNumber, message)) }
            return
        }
        val link = buildWhatsAppLinkUseCase(whatsAppNumber, message)
        launchWhatsApp(context, target, link)
    }

    fun onWhatsAppPickerConfirmed(context: Context, target: WhatsAppTarget, remember: Boolean) {
        val sheet = _uiState.value.activeSheet as? HomeSheet.WhatsAppPicker ?: return
        _uiState.update { it.copy(activeSheet = null) }
        if (remember) {
            viewModelScope.launch { settingsRepository.setWhatsAppTarget(target) }
        }
        val link = buildWhatsAppLinkUseCase(sheet.whatsAppNumber, sheet.message)
        launchWhatsApp(context, target, link)
    }

    private fun launchWhatsApp(context: Context, target: WhatsAppTarget, link: String) {
        when (WhatsAppLauncher.open(context, target, link)) {
            is WhatsAppLaunchResult.Success -> rememberCurrentNumber()
            is WhatsAppLaunchResult.NotInstalled -> {
                val alternative = if (target == WhatsAppTarget.REGULAR) WhatsAppTarget.BUSINESS else WhatsAppTarget.REGULAR
                val alternativeInstalled = WhatsAppLauncher.isInstalled(context, alternative)
                _uiState.update {
                    it.copy(
                        activeDialog = if (!alternativeInstalled) {
                            HomeDialog.BothWhatsAppMissing
                        } else {
                            HomeDialog.AppNotInstalled(target, alternative, alternativeInstalled)
                        },
                    )
                }
            }
        }
    }

    fun onOpenAlternativeWhatsApp(context: Context) {
        val dialog = _uiState.value.activeDialog as? HomeDialog.AppNotInstalled ?: return
        _uiState.update { it.copy(activeDialog = null) }
        val fallbackTarget = dialog.fallbackTarget ?: return
        if (!dialog.fallbackInstalled) return
        val result = _uiState.value.phoneResult ?: return
        val whatsAppNumber = result.whatsAppNumber ?: return
        val message = _uiState.value.preparedMessage.ifBlank { null }
        val link = buildWhatsAppLinkUseCase(whatsAppNumber, message)
        launchWhatsApp(context, fallbackTarget, link)
    }

    fun onOpenPlayStoreForMissingApp(context: Context) {
        val dialog = _uiState.value.activeDialog as? HomeDialog.AppNotInstalled ?: return
        _uiState.update { it.copy(activeDialog = null) }
        val packageName = WhatsAppLauncher.packageNameFor(dialog.missingTarget) ?: return
        openUrl(context, WhatsAppLauncher.playStoreLink(packageName))
    }

    fun onSaveContactClicked(context: Context) {
        val result = _uiState.value.phoneResult ?: return
        val e164 = result.e164?.takeIf { result.isActionable } ?: return
        when (ContactLauncher.open(context, _uiState.value.contactName, e164)) {
            ContactLaunchResult.Success -> rememberCurrentNumber()
            ContactLaunchResult.NoContactsApp ->
                _uiState.update { it.copy(activeDialog = HomeDialog.GenericError(R.string.error_no_contacts_app)) }
        }
    }

    fun onShareVCardClicked(context: Context, fallbackName: String) {
        val result = _uiState.value.phoneResult ?: return
        val e164 = result.e164?.takeIf { result.isActionable } ?: return
        val vCardContent = generateVCardUseCase(_uiState.value.contactName, e164, fallbackName)
        val fileHint = _uiState.value.contactName.ifBlank { fallbackName }
        val uri = VCardGenerator.writeToCache(context, vCardContent, fileHint)
        if (uri == null) {
            _uiState.update { it.copy(activeDialog = HomeDialog.GenericError(R.string.error_vcard_failed)) }
            return
        }
        val chooser = Intent.createChooser(VCardGenerator.buildShareIntent(uri), null)
        try {
            context.startActivity(chooser)
            rememberCurrentNumber()
        } catch (e: ActivityNotFoundException) {
            _uiState.update { it.copy(activeDialog = HomeDialog.GenericError(R.string.error_no_share_app)) }
        }
    }

    fun onCopyRequested(context: Context, type: CopyType) {
        val result = _uiState.value.phoneResult?.takeIf { it.isActionable } ?: return
        val (text, messageResId) = when (type) {
            CopyType.INTERNATIONAL -> (result.internationalDisplay ?: return) to R.string.snackbar_copied_international
            CopyType.WHATSAPP -> (result.whatsAppNumber ?: return) to R.string.snackbar_copied_whatsapp
            CopyType.E164 -> (result.e164 ?: return) to R.string.snackbar_copied_e164
        }
        ClipboardHelper.copy(context, "phone_number", text)
        _uiState.update { it.copy(activeSheet = null) }
        viewModelScope.launch { _snackbarEvents.emit(HomeSnackbarEvent.ResourceMessage(messageResId)) }
    }

    fun onShareTextRequested(context: Context, withMessage: Boolean) {
        val result = _uiState.value.phoneResult?.takeIf { it.isActionable } ?: return
        val number = result.internationalDisplay ?: return
        val message = _uiState.value.preparedMessage
        val text = if (withMessage && message.isNotBlank()) "$number\n$message" else number
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        _uiState.update { it.copy(activeSheet = null) }
        runCatching { context.startActivity(Intent.createChooser(intent, null)) }
    }

    private fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        runCatching { context.startActivity(intent) }
    }

    private fun rememberCurrentNumber() {
        val state = _uiState.value
        val e164 = state.phoneResult?.e164 ?: return
        if (!keepRecentNumbersHistory) return
        viewModelScope.launch {
            recentsRepository.pushRecentNumber(
                RecentNumberEntry(e164 = e164, name = state.contactName),
                historyEnabled = true,
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DirectNumberApp
                HomeViewModel(
                    settingsRepository = app.settingsRepository,
                    recentsRepository = app.recentsRepository,
                    countryRepository = app.countryRepository,
                    validatePhoneNumberUseCase = app.validatePhoneNumberUseCase,
                    buildWhatsAppLinkUseCase = app.buildWhatsAppLinkUseCase,
                    generateVCardUseCase = app.generateVCardUseCase,
                )
            }
        }
    }
}
