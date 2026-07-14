package com.directnumber.app.domain.usecase

/**
 * Produces vCard 3.0 text content. Writing it to a temp file and sharing it via FileProvider
 * is a platform concern handled by util.VCardGenerator — this class only builds the string,
 * which keeps it plain-JVM unit-testable.
 */
class GenerateVCardUseCase {
    operator fun invoke(name: String, e164Number: String, fallbackName: String): String {
        val safeName = name.ifBlank { fallbackName }
        return buildString {
            append("BEGIN:VCARD\r\n")
            append("VERSION:3.0\r\n")
            append("FN:${escape(safeName)}\r\n")
            append("TEL;TYPE=CELL:${escape(e164Number)}\r\n")
            append("END:VCARD\r\n")
        }
    }

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n")
}
