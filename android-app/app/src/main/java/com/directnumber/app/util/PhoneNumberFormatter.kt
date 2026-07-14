package com.directnumber.app.util

/**
 * Live input sanitizer for the phone number field. Keeps only characters a phone number can
 * legitimately contain — digits, a single leading "+", spaces, hyphens and parentheses — and
 * silently drops anything else (letters, emoji, stray symbols) as the user types or pastes.
 * Semantic parsing/validation is a separate concern, handled by libphonenumber in the domain layer.
 */
object PhoneNumberFormatter {

    private val ALLOWED_PUNCTUATION = charArrayOf(' ', '-', '(', ')')

    fun sanitizeInput(raw: String): String {
        val builder = StringBuilder()
        var plusConsumed = false
        var hasDigit = false
        for (char in raw) {
            when {
                char == '+' && !hasDigit && !plusConsumed -> {
                    builder.append(char)
                    plusConsumed = true
                }
                char.isDigit() -> {
                    builder.append(char)
                    hasDigit = true
                }
                // Punctuation is only kept once real content has started, so leading junk
                // (e.g. a space before "+54..." pasted from "Tel: +54...") can't push the
                // "+" check above past its "no content yet" guard and get itself dropped.
                char in ALLOWED_PUNCTUATION && (hasDigit || plusConsumed) -> builder.append(char)
                else -> Unit
            }
        }
        return builder.toString()
    }

    /** Strips everything but digits and a leading "+" — useful before handing text to libphonenumber. */
    fun stripToDigitsAndPlus(raw: String): String {
        val builder = StringBuilder()
        raw.forEachIndexed { index, char ->
            if (char.isDigit() || (char == '+' && index == 0)) builder.append(char)
        }
        return builder.toString()
    }
}
