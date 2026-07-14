package com.directnumber.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumberFormatterTest {

    @Test
    fun `keeps digits spaces dashes and parentheses`() {
        assertEquals("+54 (11) 5555-1234", PhoneNumberFormatter.sanitizeInput("+54 (11) 5555-1234"))
    }

    @Test
    fun `strips letters pasted alongside a number`() {
        assertEquals("+54 11 2345-6789", PhoneNumberFormatter.sanitizeInput("Tel: +54 11 2345-6789"))
    }

    @Test
    fun `only the first plus sign is kept`() {
        assertEquals("+5491123456789", PhoneNumberFormatter.sanitizeInput("+54+91+123456789"))
    }

    @Test
    fun `plus sign not at the start is dropped`() {
        assertEquals("54911234567", PhoneNumberFormatter.sanitizeInput("54+911234567"))
    }

    @Test
    fun `emoji and symbols are removed`() {
        assertEquals("966512345678", PhoneNumberFormatter.sanitizeInput("📞966512345678✅"))
    }

    @Test
    fun `stripToDigitsAndPlus removes formatting punctuation`() {
        assertEquals("+5491123456789", PhoneNumberFormatter.stripToDigitsAndPlus("+54 (9) 11-2345-6789"))
    }
}
