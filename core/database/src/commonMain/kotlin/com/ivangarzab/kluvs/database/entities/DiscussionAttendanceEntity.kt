package com.ivangarzab.kluvs.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for the member's own RSVP status on a discussion.
 * Represents a cached attendance status with TTL tracking.
 *
 * Note: this only caches the caller's own status, not the full attendance
 * roster (which includes other members' responses and names).
 */
@Entity(tableName = "discussion_attendance")
data class DiscussionAttendanceEntity(
    @PrimaryKey val discussionId: String,
    val status: String, // AttendanceStatus enum name
    val lastFetchedAt: Long // Timestamp in milliseconds for TTL check
)
