package com.ivangarzab.kluvs.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for the member's reading progress on a book.
 * Represents a cached progress entry with TTL tracking.
 * Maps to ReadingProgress from the API.
 */
@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val bookId: String,
    val sessionId: String?,
    val type: String, // ProgressType enum name
    val status: String, // ProgressStatus enum name
    val currentPage: Int?,
    val percentComplete: Float?,
    val startedAt: String?, // ISO-8601 datetime string
    val completedAt: String?, // ISO-8601 datetime string
    val lastFetchedAt: Long // Timestamp in milliseconds for TTL check
)
