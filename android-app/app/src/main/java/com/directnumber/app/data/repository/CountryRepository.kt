package com.directnumber.app.data.repository

import com.directnumber.app.domain.model.Country
import com.directnumber.app.domain.validator.PhoneNumberValidator
import java.util.Locale

/**
 * Supplies country/region data. The full list and every dial code come straight from
 * libphonenumber's numbering-plan metadata — nothing here is a hand-maintained table of
 * country codes, so it stays correct as the underlying metadata is updated.
 */
class CountryRepository(
    private val validator: PhoneNumberValidator = PhoneNumberValidator(),
) {
    /** Priority row shown above the fold, in the exact order requested by product. */
    val priorityRegionCodes: List<String> = listOf("AR", "SY", "RU", "US", "SA", "AE", "QA", "JO")

    /** Remaining Arab countries shown in the quick-pick grid, after the priority row. */
    val otherArabRegionCodes: List<String> = listOf(
        "KW", "BH", "OM", "YE", "IQ", "LB", "PS", "EG", "LY", "TN", "DZ", "MA", "SD", "MR", "SO", "DJ", "KM",
    )

    val quickPickRegionCodes: List<String> = priorityRegionCodes + otherArabRegionCodes

    private val allCountriesCache: List<Country> by lazy {
        validator.supportedRegions()
            .filter { it != Country.WORLD_REGION }
            .map { region -> Country(regionCode = region, dialCode = validator.dialCodeForRegion(region)) }
    }

    fun getCountry(regionCode: String): Country? =
        allCountriesCache.firstOrNull { it.regionCode == regionCode }

    fun allCountriesSortedByName(locale: Locale): List<Country> =
        allCountriesCache.sortedBy { it.displayName(locale).lowercase(locale) }

    fun search(query: String, locale: Locale): List<Country> {
        if (query.isBlank()) return allCountriesSortedByName(locale)
        val normalizedQuery = query.trim().removePrefix("+")
        return allCountriesCache.filter { country ->
            country.displayName(locale).contains(query, ignoreCase = true) ||
                country.dialCode.toString().startsWith(normalizedQuery) ||
                country.regionCode.contains(query, ignoreCase = true)
        }.sortedBy { it.displayName(locale).lowercase(locale) }
    }
}
