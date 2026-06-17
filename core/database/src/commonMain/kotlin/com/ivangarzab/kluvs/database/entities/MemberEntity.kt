package com.ivangarzab.kluvs.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for club members.
 * Represents a cached member with TTL tracking.
 * Indexed by userId for efficient lookups.
 * Maps to MemberDto from the API.
 */
@Entity(
    tableName = "members",
    indices = [Index("userId")]
)
data class MemberEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val name: String?,
    val handle: String?,
    val avatarPath: String?,
    val booksRead: Int,
    val createdAt: String?, // ISO-8601 datetime string
    val lastFetchedAt: Long // Timestamp in milliseconds for TTL check
)
