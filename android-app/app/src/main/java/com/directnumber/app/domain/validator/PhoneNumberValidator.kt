package com.directnumber.app.domain.validator

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber

/**
 * Thin, testable wrapper around Google libphonenumber. This is the only place in the app
 * that talks to the library directly — no hand-rolled formatting rules, including for
 * Argentina's mobile-number quirks (the leading "9" / trunk "0"/"15" handling is entirely
 * delegated to libphonenumber's numbering-plan metadata).
 */
class PhoneNumberValidator(
    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance(),
) {

    sealed class ParseOutcome {
        data class Parsed(
            val number: PhoneNumber,
            val isValid: Boolean,
            val isPossible: Boolean,
            val detectedRegionCode: String?,
        ) : ParseOutcome()

        data class Failed(val reason: FailureReason) : ParseOutcome()
    }

    enum class FailureReason { EMPTY, TOO_SHORT, NOT_A_NUMBER, INVALID_COUNTRY_CODE, UNKNOWN }

    fun parse(rawInput: String, defaultRegion: String): ParseOutcome {
        val sanitized = rawInput.trim()
        if (sanitized.isEmpty()) return ParseOutcome.Failed(FailureReason.EMPTY)

        return try {
            val number = phoneUtil.parse(sanitized, defaultRegion)
            val isValid = phoneUtil.isValidNumber(number)
            val isPossible = phoneUtil.isPossibleNumber(number)
            val detectedRegion = phoneUtil.getRegionCodeForNumber(number)
            ParseOutcome.Parsed(number, isValid, isPossible, detectedRegion)
        } catch (e: NumberParseException) {
            val reason = when (e.errorType) {
                NumberParseException.ErrorType.TOO_SHORT_AFTER_IDD,
                NumberParseException.ErrorType.TOO_SHORT_NSN,
                -> FailureReason.TOO_SHORT
                NumberParseException.ErrorType.NOT_A_NUMBER -> FailureReason.NOT_A_NUMBER
                NumberParseException.ErrorType.INVALID_COUNTRY_CODE -> FailureReason.INVALID_COUNTRY_CODE
                else -> FailureReason.UNKNOWN
            }
            ParseOutcome.Failed(reason)
        }
    }

    fun toE164(number: PhoneNumber): String =
        phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164)

    fun toInternationalDisplay(number: PhoneNumber): String =
        phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)

    /** Digits only, no leading "+" — the format wa.me links require. */
    fun toWhatsAppNumber(number: PhoneNumber): String = toE164(number).removePrefix("+")

    fun numberType(number: PhoneNumber): PhoneNumberUtil.PhoneNumberType = phoneUtil.getNumberType(number)

    fun supportedRegions(): Set<String> = phoneUtil.supportedRegions

    fun dialCodeForRegion(regionCode: String): Int = phoneUtil.getCountryCodeForRegion(regionCode)
}
