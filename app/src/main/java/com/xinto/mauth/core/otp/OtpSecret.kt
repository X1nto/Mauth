package com.xinto.mauth.core.otp

// Lengths a Base32 encoder can produce, modulo 8
// https://www.rfc-editor.org/rfc/rfc4648#section-6
private val VALID_LENGTHS = setOf(0, 2, 4, 5, 7)

/**
 * @return the secret in spec form (uppercase, no separators, no padding), or null when it isn't valid Base32.
 */
fun String.normalizeOtpSecret(): String? {
    val cleaned = trim()
        .replace(" ", "")
        .replace("-", "")
        .trimEnd('=')
        .uppercase()

    if (cleaned.isEmpty())
        return null

    if (cleaned.any { it !in 'A'..'Z' && it !in '2'..'7' })
        return null

    if (cleaned.length % 8 !in VALID_LENGTHS)
        return null

    return cleaned
}
