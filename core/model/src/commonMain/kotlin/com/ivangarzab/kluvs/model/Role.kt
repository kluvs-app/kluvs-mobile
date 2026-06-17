package com.ivangarzab.kluvs.model

/**
 * Represents a member's role within a club.
 */
enum class Role() {
    OWNER,
    ADMIN,
    MEMBER;

    companion object {
        /**
         * Converts a string role name to a Role enum.
         * Case-insensitive. Defaults to MEMBER for unknown values.
         *
         * @param value The string representation of the role
         * @return The corresponding Role enum value
         */
        fun fromString(value: String): Role = when (value.lowercase()) {
            "owner" -> OWNER
            "admin" -> ADMIN
            "member" -> MEMBER
            else -> MEMBER
        }
    }
}
