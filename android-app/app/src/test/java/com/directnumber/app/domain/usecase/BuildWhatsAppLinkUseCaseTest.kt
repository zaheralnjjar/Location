package com.directnumber.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BuildWhatsAppLinkUseCaseTest {

    private val useCase = BuildWhatsAppLinkUseCase()

    @Test
    fun `link without a message has no query string`() {
        val link = useCase("5491123456789", null)
        assertEquals("https://wa.me/5491123456789", link)
    }

    @Test
    fun `blank message is treated as no message`() {
        val link = useCase("5491123456789", "   ")
        assertEquals("https://wa.me/5491123456789", link)
    }

    @Test
    fun `message is URL-encoded with spaces as percent-20`() {
        val link = useCase("5491123456789", "Hello there")
        assertEquals("https://wa.me/5491123456789?text=Hello%20there", link)
    }

    @Test
    fun `arabic message is percent-encoded`() {
        val link = useCase("963944567890", "السلام عليكم")
        assertEquals(
            "https://wa.me/963944567890?text=%D8%A7%D9%84%D8%B3%D9%84%D8%A7%D9%85%20%D8%B9%D9%84%D9%8A%D9%83%D9%85",
            link,
        )
    }

    @Test
    fun `rejects a number that still has a plus sign`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase("+5491123456789", null)
        }
    }

    @Test
    fun `rejects a number with non-digit characters`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase("549 11 2345 6789", null)
        }
    }
}
