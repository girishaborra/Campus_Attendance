package com.example.campus_attendance.util

import java.util.Locale

/**
 * Validates if an email address belongs to an official student with a valid registration number.
 * Valid email format: <regNo>@cutmap.ac.in
 * - regNo is exactly 12 digits.
 * - regNo must be between 231801370001 and 231801370095 inclusive.
 * - Domain must be exactly cutmap.ac.in (case-insensitive; trim spaces and lowercase before validating).
 */
fun isValidCollegeEmail(email: String): Boolean {
    val cleanedEmail = email.trim().lowercase(Locale.ROOT)
    val regex = Regex("""^(\d{12})@""" + Regex.escape(EMAIL_DOMAIN) + "$")
    val matchResult = regex.matchEntire(cleanedEmail) ?: return false
    val regNo = matchResult.groupValues[1].toLongOrNull() ?: return false
    return regNo in REG_NO_MIN..REG_NO_MAX
}

/**
 * Extracts the 12-digit registration number from a valid college email address.
 * Returns null if the email is not a valid college email.
 */
fun extractRegNo(email: String): String? {
    val cleanedEmail = email.trim().lowercase(Locale.ROOT)
    if (!isValidCollegeEmail(cleanedEmail)) return null
    return cleanedEmail.substringBefore("@")
}
