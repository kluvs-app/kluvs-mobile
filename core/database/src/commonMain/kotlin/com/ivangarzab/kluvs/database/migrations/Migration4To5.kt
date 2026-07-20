package com.ivangarzab.kluvs.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Migration from database version 4 to 5.
 *
 * Adds the `session_members` table caching each session's participation list
 * (per-member `is_reading` flags from the club response's `active_session.members`).
 *
 * Background:
 * The Clubs screen surfaces reading/skipping status per member and the
 * end-session flow previews how many readers get credit. Clubs are served
 * from cache for 24h, so this data must be persisted alongside the session
 * for those indicators to survive cached loads.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `session_members` (
                `sessionId` TEXT NOT NULL,
                `memberId` TEXT NOT NULL,
                `memberName` TEXT,
                `isReading` INTEGER NOT NULL,
                PRIMARY KEY(`sessionId`, `memberId`)
            )
            """.trimIndent()
        )
    }
}
