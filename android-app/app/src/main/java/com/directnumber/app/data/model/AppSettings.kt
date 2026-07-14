package com.directnumber.app.data.model

enum class WhatsAppTarget { REGULAR, BUSINESS, ASK_EVERY_TIME }

enum class AppLanguageOption { SYSTEM, ARABIC, SPANISH, ENGLISH }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val defaultRegionCode: String = "AR",
    val lastUsedRegionCode: String? = null,
    val rememberLastCountry: Boolean = true,
    val showRecentCountries: Boolean = true,
    val whatsAppTarget: WhatsAppTarget = WhatsAppTarget.ASK_EVERY_TIME,
    val language: AppLanguageOption = AppLanguageOption.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val keepRecentNumbersHistory: Boolean = false,
) {
    /** The country used to pre-select on the home screen: last-used if remembered, else the default. */
    fun startingRegionCode(): String =
        if (rememberLastCountry) lastUsedRegionCode ?: defaultRegionCode else defaultRegionCode
}
