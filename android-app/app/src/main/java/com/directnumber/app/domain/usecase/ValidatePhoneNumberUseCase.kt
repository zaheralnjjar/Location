package com.directnumber.app.domain.usecase

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.directnumber.app.domain.model.NumberType
import com.directnumber.app.domain.model.PhoneNumberResult
import com.directnumber.app.domain.model.ValidationStatus
import com.directnumber.app.domain.validator.PhoneNumberValidator

/**
 * Validates and formats the number the user is typing.
 *
 * @param selectedRegionCode the country the user picked in the UI.
 * @param forceSelectedRegion when the raw input carries its own "+<code>" that resolves to a
 *   different region than [selectedRegionCode], and the user explicitly chose to keep
 *   [selectedRegionCode] anyway (dismissing the conflict prompt), pass true. libphonenumber
 *   always trusts an embedded "+<code>" over the supplied default region, so honoring the
 *   user's choice requires stripping the leading "+" first and re-parsing the remaining digits
 *   as a plain national number for [selectedRegionCode] — which will often, correctly, come out
 *   invalid, since the digits really were dialled for a different country.
 */
class ValidatePhoneNumberUseCase(
    private val validator: PhoneNumberValidator = PhoneNumberValidator(),
) {
    operator fun invoke(
        rawInput: String,
        selectedRegionCode: String,
        forceSelectedRegion: Boolean = false,
    ): PhoneNumberResult {
        val parseInput = if (forceSelectedRegion) rawInput.trim().removePrefix("+") else rawInput

        when (val outcome = validator.parse(parseInput, selectedRegionCode)) {
            is PhoneNumberValidator.ParseOutcome.Failed -> {
                val status = when (outcome.reason) {
                    PhoneNumberValidator.FailureReason.EMPTY,
                    PhoneNumberValidator.FailureReason.TOO_SHORT,
                    -> ValidationStatus.INCOMPLETE
                    else -> ValidationStatus.INVALID
                }
                return PhoneNumberResult(status = status, selectedRegionCode = selectedRegionCode)
            }

            is PhoneNumberValidator.ParseOutcome.Parsed -> {
                val looksInternational = !forceSelectedRegion && rawInput.trim().startsWith("+")
                val detectedRegion = outcome.detectedRegionCode
                val hasConflict = looksInternational && detectedRegion != null && detectedRegion != selectedRegionCode

                val status = when {
                    hasConflict && outcome.isValid -> ValidationStatus.NEEDS_REVIEW
                    outcome.isValid -> ValidationStatus.VALID
                    outcome.isPossible -> ValidationStatus.INVALID
                    else -> ValidationStatus.INCOMPLETE
                }

                if (status != ValidationStatus.VALID) {
                    return PhoneNumberResult(
                        status = status,
                        detectedRegionCode = detectedRegion,
                        selectedRegionCode = selectedRegionCode,
                    )
                }

                val numberType = when (validator.numberType(outcome.number)) {
                    PhoneNumberUtil.PhoneNumberType.MOBILE,
                    PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE,
                    -> NumberType.MOBILE
                    PhoneNumberUtil.PhoneNumberType.FIXED_LINE -> NumberType.LANDLINE
                    else -> NumberType.UNKNOWN
                }

                return PhoneNumberResult(
                    status = ValidationStatus.VALID,
                    e164 = validator.toE164(outcome.number),
                    internationalDisplay = validator.toInternationalDisplay(outcome.number),
                    whatsAppNumber = validator.toWhatsAppNumber(outcome.number),
                    numberType = numberType,
                    detectedRegionCode = detectedRegion,
                    selectedRegionCode = selectedRegionCode,
                )
            }
        }
    }
}
