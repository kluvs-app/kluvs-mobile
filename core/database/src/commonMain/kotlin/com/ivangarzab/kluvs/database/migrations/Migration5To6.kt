package com.ivangarzab.kluvs.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Migration from database version 5 to 6.
 *
 * Adds the `book_enrichment` table caching book-details enrichment responses (volume info,
 * author info, other books by the author) so the book details screen can render them offline.
 * Nested objects are stored as JSON text columns. Purely additive: existing data is untouched.
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `book_enrichment` (
                `cacheKey` TEXT NOT NULL,
                `volumeInfoJson` TEXT,
                `authorJson` TEXT,
                `authorBooksJson` TEXT NOT NULL,
                `lastFetchedAt` INTEGER NOT NULL,
                PRIMARY KEY(`cacheKey`)
            )
            """.trimIndent()
        )
    }
}
