package com.ivangarzab.kluvs.ui.clubs

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Builds a [LocalDateTime] from a date picker's epoch millis and a time picker's hour/minute.
 *
 * Extracts year/month/day from the millis (UTC), combines with the provided hour and minute.
 */
@OptIn(ExperimentalTime::class)
internal fun buildLocalDateTime(epochMillis: Long, hour: Int, minute: Int): LocalDateTime {
    val date = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.UTC)
    return LocalDateTime(
        year = date.year,
        month = date.month,
        day = date.day,
        hour = hour.coerceIn(0, 23),
        minute = minute.coerceIn(0, 59)
    )
}

/**
 * Formats epoch millis from a DatePicker into a human-readable date string (YYYY-MM-DD).
 */
@OptIn(ExperimentalTime::class)
internal fun formatDateMillis(epochMillis: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.UTC)
    return "${dt.year}-${dt.month.toString().padStart(2, '0')}-${dt.day.toString().padStart(2, '0')}"
}

/**
 * Converts a [LocalDateTime] to the epoch millis expected by DatePickerState.
 *
 * Returns midnight UTC for the date component, which is what the DatePicker
 * uses to represent a selected day regardless of local timezone.
 */
@OptIn(ExperimentalTime::class)
internal fun localDateTimeToDateMillis(localDateTime: LocalDateTime): Long {
    // Build midnight UTC for the date component, then convert to epoch millis
    val midnight = LocalDateTime(
        year = localDateTime.year,
        month = localDateTime.month,
        day = localDateTime.day,
        hour = 0,
        minute = 0
    )
    return midnight.toInstant(TimeZone.UTC).toEpochMilliseconds()
}
