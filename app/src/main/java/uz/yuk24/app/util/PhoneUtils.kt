package uz.yuk24.app.util

object PhoneUtils {

    /**
     * Normalises an input phone number to the canonical "+998XXXXXXXXX" form
     * the backend expects (spec §5.8).
     */
    fun normalize(input: String): String {
        val digits = input.filter { it.isDigit() }
        return when {
            digits.startsWith("998") && digits.length == 12 -> "+$digits"
            digits.length == 9 -> "+998$digits"
            else -> "+998$digits"
        }
    }

    /**
     * Strips a phone number to the 9 local digits (without country code) for use
     * inside the user input field.
     */
    fun stripLocal(input: String): String {
        val digits = input.filter { it.isDigit() }
        return if (digits.startsWith("998")) digits.drop(3).take(9) else digits.take(9)
    }

    /**
     * Returns true if the 9-digit local body is complete.
     */
    fun isComplete(localDigits: String): Boolean = localDigits.length == 9

    /**
     * Formats raw digits into "XX XXX XX XX" mask for display while typing.
     */
    fun formatMask(localDigits: String): String {
        val d = localDigits.take(9)
        val sb = StringBuilder()
        for ((i, c) in d.withIndex()) {
            if (i == 2 || i == 5 || i == 7) sb.append(' ')
            sb.append(c)
        }
        return sb.toString()
    }

    /**
     * Returns a pretty form for display: "+998 (90) 123-45-67".
     */
    fun displayPretty(localDigits: String): String {
        val d = localDigits.take(9).padEnd(0)
        if (d.isEmpty()) return "+998"
        return buildString {
            append("+998")
            if (d.length >= 2) append(" (").append(d.substring(0, 2)).append(')')
            if (d.length >= 3) append(' ').append(d.substring(2, minOf(5, d.length)))
            if (d.length >= 6) append('-').append(d.substring(5, minOf(7, d.length)))
            if (d.length >= 8) append('-').append(d.substring(7, minOf(9, d.length)))
        }
    }
}
