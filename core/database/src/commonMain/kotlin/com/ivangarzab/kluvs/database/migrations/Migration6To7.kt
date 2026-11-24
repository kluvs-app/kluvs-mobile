package com.ivangarzab.kluvs.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Migration from database version 6 to 7.
 *
 * Adds the `progress_query` table, which records when each progress query (book, session and
 * status filter) was last fetched. Freshness used to live only on progress rows, so a query that
 * returned no rows could never be a cache hit. Purely additive: existing data is untouched.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `progress_query` (
                `queryKey` TEXT NOT NULL,
                `lastFetchedAt` INTEGER NOT NULL,
                PRIMARY KEY(`queryKey`)
            )
            """.trimIndent()
        )
    }
}
