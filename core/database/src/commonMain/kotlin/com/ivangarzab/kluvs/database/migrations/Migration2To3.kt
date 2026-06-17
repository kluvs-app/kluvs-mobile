package com.ivangarzab.kluvs.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Migration from database version 2 to 3.
 *
 * Changes:
 * - Remove `role` column from `members` table (role is now per-club, not per-member)
 * - Add `role` column to `club_members` table (tracks member's role in each club)
 *
 * Background:
 * The backend API moved the role field from the members table to the memberclubs join table,
 * enabling per-club roles. A member can now be an owner in one club and a regular member in another.
 *
 * Data Migration:
 * No data migration is performed. All existing club-member relationships default to "member" role.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        // 1. Recreate members table without role column
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `members_new` (
                `id` TEXT NOT NULL,
                `userId` TEXT,
                `name` TEXT,
                `handle` TEXT,
                `avatarPath` TEXT,
                `booksRead` INTEGER NOT NULL,
                `createdAt` TEXT,
                `lastFetchedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            INSERT INTO `members_new` (id, userId, name, handle, avatarPath, booksRead, createdAt, lastFetchedAt)
            SELECT id, userId, name, handle, avatarPath, booksRead, createdAt, lastFetchedAt
            FROM `members`
            """.trimIndent()
        )

        connection.execSQL("DROP TABLE `members`")
        connection.execSQL("ALTER TABLE `members_new` RENAME TO `members`")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_members_userId` ON `members` (`userId`)")

        // 2. Add role column to club_members table
        connection.execSQL("ALTER TABLE `club_members` ADD COLUMN `role` TEXT NOT NULL DEFAULT 'member'")
    }
}
