package com.ivangarzab.kluvs.data.domain

/**
 * A release version (MAJOR.MINOR.PATCH), ordered numerically so `1.10.0` is newer than `1.9.0`.
 */
internal data class AppVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Comparable<AppVersion> {

    override fun compareTo(other: AppVersion): Int =
        compareValuesBy(this, other, AppVersion::major, AppVersion::minor, AppVersion::patch)

    companion object {
        /**
         * Parses `MAJOR.MINOR.PATCH`. Missing trailing parts count as 0 (`1.2` is `1.2.0`).
         * Returns null for anything else, e.g. an empty string or a non-numeric part.
         */
        fun parse(value: String): AppVersion? {
            val parts = value.trim().split('.')
            if (parts.size !in 1..3) return null
            val numbers = parts.map { part ->
                if (part.isEmpty() || !part.all { it.isDigit() }) return null
                part.toIntOrNull() ?: return null
            }
            return AppVersion(
                major = numbers[0],
                minor = numbers.getOrElse(1) { 0 },
                patch = numbers.getOrElse(2) { 0 }
            )
        }
    }
}
