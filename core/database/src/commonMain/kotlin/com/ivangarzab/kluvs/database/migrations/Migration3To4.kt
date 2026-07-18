package com.ivangarzab.kluvs.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Migration from database version 3 to 4.
 *
 * Adds local caching tables for the newly polished member-scoped API entities:
 * shelf, likes, progress, discussion notes, and discussion attendance (RSVP).
 *
 * Background:
 * These entities previously hit the remote API on every read with no offline
 * support. This migration adds the tables backing their Room-based cache-aside
 * repositories, bringing them in line with Sessions/Clubs/Members/Books/Servers.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `shelf` (
                `bookId` TEXT NOT NULL,
                `shelf` TEXT NOT NULL,
                `source` TEXT NOT NULL,
                `updatedAt` TEXT,
                `lastFetchedAt` INTEGER NOT NULL,
                PRIMARY KEY(`bookId`)
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `likes` (
                `bookId` TEXT NOT NULL,
                `liked` INTEGER NOT NULL,
                `lastFetchedAt` INTEGER NOT NULL,
                PRIMARY KEY(`bookId`)
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `progress` (
                `id` TEXT NOT NULL,
                `memberId` TEXT NOT NULL,
                `bookId` TEXT NOT NULL,
                `sessionId` TEXT,
                `type` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `currentPage` INTEGER,
                `percentComplete` REAL,
                `startedAt` TEXT,
                `completedAt` TEXT,
                `lastFetchedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `discussion_notes` (
                `discussionId` TEXT NOT NULL,
                `id` TEXT NOT NULL,
                `memberId` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `visibility` TEXT NOT NULL,
                `createdAt` TEXT,
                `updatedAt` TEXT,
                `lastFetchedAt` INTEGER NOT NULL,
                PRIMARY KEY(`discussionId`)
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `discussion_attendance` (
                `discussionId` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `lastFetchedAt` INTEGER NOT NULL,
                PRIMARY KEY(`discussionId`)
            )
            """.trimIndent()
        )
    }
}
