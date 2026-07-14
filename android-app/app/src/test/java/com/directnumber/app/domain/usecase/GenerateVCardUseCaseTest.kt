package com.directnumber.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateVCardUseCaseTest {

    private val useCase = GenerateVCardUseCase()

    @Test
    fun `vcard contains name and E164 number`() {
        val vcard = useCase("Ahmad", "+5491123456789", "New Contact")
        assertEquals(
            "BEGIN:VCARD\r\nVERSION:3.0\r\nFN:Ahmad\r\nTEL;TYPE=CELL:+5491123456789\r\nEND:VCARD\r\n",
            vcard,
        )
    }

    @Test
    fun `blank name falls back to the provided default`() {
        val vcard = useCase("", "+963944567890", "New Contact")
        assertTrue(vcard.contains("FN:New Contact"))
    }

    @Test
    fun `special characters in the name are escaped per RFC 6350`() {
        val vcard = useCase("Smith; Jones, Co\\Ltd", "+12015550123", "New Contact")
        assertTrue(vcard.contains("FN:Smith\\; Jones\\, Co\\\\Ltd"))
    }

    @Test
    fun `arabic name is preserved as-is`() {
        val vcard = useCase("مطعم الشام", "+963944567890", "جهة اتصال جديدة")
        assertTrue(vcard.contains("FN:مطعم الشام"))
    }
}
