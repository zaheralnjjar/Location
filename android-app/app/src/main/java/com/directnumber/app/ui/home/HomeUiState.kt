package com.directnumber.app.ui.home

import com.directnumber.app.data.model.WhatsAppTarget
import com.directnumber.app.domain.model.Country
import com.directnumber.app.domain.model.PhoneNumberResult

sealed interface HomeSheet {
    data object CountryPicker : HomeSheet
    data object CopyActions : HomeSheet
    data class WhatsAppPicker(val whatsAppNumber: String, val message: String?) : HomeSheet
}

sealed interface HomeDialog {
    data class AppNotInstalled(
        val missingTarget: WhatsAppTarget,
        val fallbackTarget: WhatsAppTarget?,
        val fallbackInstalled: Boolean,
    ) : HomeDialog
    data object BothWhatsAppMissing : HomeDialog
    data class GenericError(val messageResId: Int) : HomeDialog
}

data class HomeUiState(
    val contactName: String = "",
    val selectedRegionCode: String = "AR",
    val selectedCountry: Country? = null,
    val rawPhoneInput: String = "",
    val preparedMessage: String = "",
    val phoneResult: PhoneNumberResult? = null,
    val quickPickCountries: List<Country> = emptyList(),
    val recentRegionCodes: List<String> = emptyList(),
    val showRecentCountries: Boolean = true,
    val whatsAppTarget: WhatsAppTarget = WhatsAppTarget.ASK_EVERY_TIME,
    val activeSheet: HomeSheet? = null,
    val activeDialog: HomeDialog? = null,
    val isReady: Boolean = false,
)

sealed interface HomeSnackbarEvent {
    data class ResourceMessage(val messageResId: Int) : HomeSnackbarEvent
}
