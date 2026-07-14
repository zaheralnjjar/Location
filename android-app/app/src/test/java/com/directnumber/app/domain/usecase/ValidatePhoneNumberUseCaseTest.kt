package com.directnumber.app.domain.usecase

import com.directnumber.app.domain.model.NumberType
import com.directnumber.app.domain.model.ValidationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Every expected value below (E.164, international format, detected region, number type) was
 * cross-checked against the real com.googlecode.libphonenumber:libphonenumber:8.13.43 library
 * before being hard-coded here, so these are not guesses at the numbering plan.
 */
class ValidatePhoneNumberUseCaseTest {

    private val useCase = ValidatePhoneNumberUseCase()

    @Test
    fun `Argentina local mobile format converts to E164 with leading 9`() {
        // Local dialing convention: trunk 0 + area code 11 + mobile prefix 15 + subscriber number.
        val result = useCase("011 15-2345-6789", "AR")
        assertEquals(ValidationStatus.VALID, result.status)
        assertEquals("+5491123456789", result.e164)
        assertEquals("+54 9 11 2345-6789", result.internationalDisplay)
        assertEquals("5491123456789", result.whatsAppNumber)
        assertEquals(NumberType.MOBILE, result.numberType)
    }

    @Test
    fun `Argentina number already in international format parses the same way`() {
        val result = useCase("+54 9 11 2345-6789", "AR")
        assertEquals(ValidationStatus.VALID, result.status)
        assertEquals("+5491123456789", result.e164)
    }

    @Test
    fun `Syria mobile number with leading 0 converts to E164`() {
        val result = useCase("0944 567 890", "SY")
        assertEquals(ValidationStatus.VALID, result.status)
        assertEquals("+963944567890", result.e164)
        assertEquals(NumberType.MOBILE, result.numberType)
    }

    @Test
    fun `Syria landline is classified as landline`() {
        val result = useCase("0112345678", "SY")
        assertEquals(ValidationStatus.VALID, result.status)
        assertEquals("+963112345678", result.e164)
        assertEquals(NumberType.LANDLINE, result.numberType)
    }

    @Test
    fun `Russia mobile number converts to E164`() {
        val result = useCase("9123456789", "RU")
        assertEquals(ValidationStatus.VALID, result.status)
        assertEquals("+79123456789", result.e164)
        assertEquals(NumberType.MOBILE, result.numberType)
    }

    @Test
    fun `United States number converts to E164`() {
        val result = useCase("2015550123", "US")
        assertEquals(ValidationStatus.VALID, result.status)
        assertEquals("+12015550123", result.e164)
    }

    @Test
    fun `Saudi Arabia mobile number with leading 0 converts to E164`() {
        val result = useCase("0512345678", "SA")
        assertEquals(ValidationStatus.VALID, result.status)
        assertEquals("+966512345678", result.e164)
        assertEquals(NumberType.MOBILE, result.numberType)
    }

    @Test
    fun `UAE mobile number converts to E164`() {
        val result = useCase("0501234567", "AE")
        assertEquals(ValidationStatus.VALID, result.status)
        assertEquals("+971501234567", result.e164)
    }

    @Test
    fun `Qatar mobile number converts to E164`() {
        val result = useCase("55123456", "QA")
        assertEquals(ValidationStatus.VALID, result.status)
        assertEquals("+97455123456", result.e164)
    }

    @Test
    fun `Jordan mobile number with leading 0 converts to E164`() {
        val result = useCase("0790123456", "JO")
        assertEquals(ValidationStatus.VALID, result.status)
        assertEquals("+962790123456", result.e164)
    }

    @Test
    fun `blank input is not validated by the use case caller`() {
        // The ViewModel is responsible for skipping the use case on blank input;
        // the use case itself reports blank input as incomplete.
        val result = useCase("", "AR")
        assertEquals(ValidationStatus.INCOMPLETE, result.status)
        assertNull(result.e164)
    }

    @Test
    fun `too short number is incomplete`() {
        val result = useCase("123", "AR")
        assertEquals(ValidationStatus.INCOMPLETE, result.status)
        assertNull(result.e164)
    }

    @Test
    fun `structurally wrong number is invalid`() {
        val result = useCase("0000000000", "US")
        assertEquals(ValidationStatus.INVALID, result.status)
        assertNull(result.e164)
    }

    @Test
    fun `number with a different embedded country code is flagged for review`() {
        val result = useCase("+1 202 555 0123", "AR")
        assertEquals(ValidationStatus.NEEDS_REVIEW, result.status)
        assertEquals("US", result.detectedRegionCode)
        assertEquals("AR", result.selectedRegionCode)
        assertEquals(true, result.hasRegionConflict)
    }

    @Test
    fun `accepting the detected region resolves the conflict as valid`() {
        val result = useCase("+1 202 555 0123", "AR", forceSelectedRegion = false)
        assertEquals(ValidationStatus.NEEDS_REVIEW, result.status)
        // Simulates HomeViewModel#onAcceptDetectedRegion, which switches selectedRegionCode
        // to the detected region and re-validates — at that point there is no conflict left.
        val resolved = useCase("+1 202 555 0123", result.detectedRegionCode!!)
        assertEquals(ValidationStatus.VALID, resolved.status)
        assertEquals("+12025550123", resolved.e164)
    }

    @Test
    fun `keeping the selected region strips the embedded country code and re-parses as a national number`() {
        // libphonenumber always trusts an embedded "+<code>" over the supplied default region,
        // so honoring "keep selected country" requires stripping the "+" first. The digits
        // "2025550123" genuinely aren't a valid Argentine number, so this correctly comes back
        // invalid rather than silently accepting a US number under the AR flag.
        val result = useCase("+1 202 555 0123", "AR", forceSelectedRegion = true)
        assertEquals(ValidationStatus.INVALID, result.status)
        assertEquals(false, result.hasRegionConflict)
        assertEquals("AR", result.selectedRegionCode)
    }

    @Test
    fun `pre-formatted international number matching the selected country has no conflict`() {
        val result = useCase("+966 51 234 5678", "SA")
        assertEquals(ValidationStatus.VALID, result.status)
        assertEquals(false, result.hasRegionConflict)
    }
}
