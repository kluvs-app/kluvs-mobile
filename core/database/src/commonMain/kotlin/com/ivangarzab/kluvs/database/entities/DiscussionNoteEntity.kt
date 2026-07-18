package com.ivangarzab.kluvs.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for the member's personal note on a discussion.
 * Represents a cached note with TTL tracking. A member has at most one
 * note per discussion, so the discussion ID is the primary key.
 * Maps to DiscussionNote from the API.
 */
@Entity(tableName = "discussion_notes")
data class DiscussionNoteEntity(
    @PrimaryKey val discussionId: String,
    val id: String, // Note ID
    val memberId: String,
    val content: String,
    val visibility: String, // NoteVisibility enum name
    val createdAt: String?, // ISO-8601 datetime string
    val updatedAt: String?, // ISO-8601 datetime string
    val lastFetchedAt: Long // Timestamp in milliseconds for TTL check
)
