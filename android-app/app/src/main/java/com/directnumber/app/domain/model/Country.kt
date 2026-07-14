package com.directnumber.app.domain.model

import java.util.Locale

/**
 * A dialable country/region. Names are never hard-coded: [displayName] resolves through
 * ICU's own localized country names, which already cover Arabic, Spanish and English.
 */
data class Country(
    val regionCode: String,
    val dialCode: Int,
) {
    fun displayName(locale: Locale): String =
        Locale.Builder().setRegion(regionCode).build().getDisplayCountry(locale)

    /** Flag emoji built from the two Regional Indicator Symbols matching [regionCode]. */
    val flagEmoji: String
        get() = regionCode.uppercase().map { char ->
            Character.toChars(0x1F1E6 + (char.code - 'A'.code))
        }.joinToString(separator = "") { String(it) }

    companion object {
        const val WORLD_REGION = "001"
    }
}
