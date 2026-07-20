package com.ivangarzab.kluvs.data.remote.mappers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime

/**
 * String extension function to parse a date string that could be either:
 * - Date-only format: "2024-12-31"
 * - DateTime format: "2024-12-31T23:17:00"
 */
fun String.parseDateString(): LocalDateTime {
    // Strip trailing timezone designator — kotlinx LocalDateTime cannot parse
    // zoned strings, but the API may return "+00:00" or "Z" suffixes.
    val normalized = this
        .removeSuffix("Z")
        .replace(Regex("""[+-]\d{2}:\d{2}$"""), "")
    return try {
        // Try parsing as full DateTime first
        LocalDateTime.parse(normalized)
    } catch (e: Exception) {
        // If that fails, parse as date-only and add midnight time
        LocalDate.parse(normalized).atTime(17, 0)
    }
}

/**
 * Lenient, null-safe variant of [parseDateString] for metadata timestamps
 * (created_at, updated_at, completed_at, ...) whose wire format the backend
 * does not guarantee. Returns null instead of throwing when the string cannot
 * be parsed.
 */
fun String?.parseDateStringOrNull(): LocalDateTime? {
    if (this.isNullOrBlank()) return null
    return try {
        this.parseDateString()
    } catch (e: Exception) {
        null
    }
}