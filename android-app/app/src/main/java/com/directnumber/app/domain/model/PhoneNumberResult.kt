package com.directnumber.app.domain.model

/**
 * Outcome of validating raw user input against a selected region using libphonenumber.
 * Every action button in the UI gates on [status] == VALID (or NEEDS_REVIEW once the
 * user explicitly confirms which country to use).
 */
data class PhoneNumberResult(
    val status: ValidationStatus,
    val e164: String? = null,
    val internationalDisplay: String? = null,
    val whatsAppNumber: String? = null,
    val numberType: NumberType = NumberType.UNKNOWN,
    val detectedRegionCode: String? = null,
    val selectedRegionCode: String,
) {
    val isActionable: Boolean
        get() = status == ValidationStatus.VALID && e164 != null

    val hasRegionConflict: Boolean
        get() = status == ValidationStatus.NEEDS_REVIEW &&
            detectedRegionCode != null &&
            detectedRegionCode != selectedRegionCode
}
