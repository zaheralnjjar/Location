package com.directnumber.app.domain.usecase

import java.net.URLEncoder

/**
 * Builds a https://wa.me/<number>?text=<message> deep link. The link never triggers a send —
 * WhatsApp always opens with the message pre-filled and waiting for the user to press send.
 */
class BuildWhatsAppLinkUseCase {
    operator fun invoke(whatsAppNumber: String, preparedMessage: String? = null): String {
        require(whatsAppNumber.isNotBlank() && whatsAppNumber.all { it.isDigit() }) {
            "whatsAppNumber must contain digits only, no '+', spaces or punctuation"
        }
        val base = "https://wa.me/$whatsAppNumber"
        val message = preparedMessage?.trim()
        if (message.isNullOrEmpty()) return base
        val encoded = URLEncoder.encode(message, "UTF-8").replace("+", "%20")
        return "$base?text=$encoded"
    }
}
