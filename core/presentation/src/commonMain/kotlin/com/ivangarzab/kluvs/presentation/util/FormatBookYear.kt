package com.ivangarzab.kluvs.presentation.util

/**
 * Formats a book's publication year for UI display.
 *
 * Years are stored as plain integers, where a negative value means BCE (e.g. Plato's *The
 * Republic* is `-380`). Those read as a bug if shown raw, so negative years render as
 * "380 BCE"; years from 0 up are shown as-is.
 *
 * Lives here, rather than in each platform's UI code, so Android, iOS, and the shared
 * use cases all format years identically.
 *
 * Examples:
 * - `-380` -> "380 BCE"
 * - `1937` -> "1937"
 */
fun formatBookYear(year: Int): String =
    if (year < 0) "${-year} BCE" else year.toString()
