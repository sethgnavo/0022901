package com.sethgnavo.a01benin

import android.graphics.Bitmap

data class Contact(
    val name: String,
    val numbers: List<String>
)

fun String.formatNumber(): String {
    // Remove all non-digit characters
    val cleaned = this.replace(Regex("\\D"), "")

    // Split into chunks of 2
    val chunks = cleaned.chunked(2)

    // Join the chunks with spaces
    return chunks.joinToString(" ")
}

fun String.getLast8Digits(): String {
    // Remove all non-digit characters
    val digitsOnly = this.replace(Regex("\\D"), "")

    // Get the last 8 characters (or fewer if the string is shorter)
    return if (digitsOnly.length > 8) digitsOnly.takeLast(8) else digitsOnly
}