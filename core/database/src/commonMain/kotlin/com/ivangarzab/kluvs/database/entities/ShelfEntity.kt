package com.ivangarzab.kluvs.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a member's shelved book.
 * Represents a cached shelf entry with TTL tracking.
 * Maps to ShelfEntry from the API.
 */
@Entity(tableName = "shelf")
data class ShelfEntity(
    @PrimaryKey val bookId: String,
    val shelf: String, // ShelfStatus enum name
    val source: String, // ShelfSource enum name
    val updatedAt: String?, // ISO-8601 datetime string
    val lastFetchedAt: Long // Timestamp in milliseconds for TTL check
)
