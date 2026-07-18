package com.ivangarzab.kluvs.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity tracking whether the member has liked a book.
 * Represents a cached like state with TTL tracking.
 */
@Entity(tableName = "likes")
data class LikeEntity(
    @PrimaryKey val bookId: String,
    val liked: Boolean,
    val lastFetchedAt: Long // Timestamp in milliseconds for TTL check
)
