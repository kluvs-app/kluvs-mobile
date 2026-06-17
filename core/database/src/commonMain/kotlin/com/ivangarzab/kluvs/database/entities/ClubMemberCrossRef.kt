package com.ivangarzab.kluvs.database.entities

import androidx.room.Entity

/**
 * Room cross-reference entity for Club-Member many-to-many relationship.
 * Tracks which members belong to which clubs and their role in each club.
 *
 * The role field represents the member's role in the specific club:
 * - "owner": Club owner
 * - "admin": Club administrator
 * - "member": Regular member (default)
 */
@Entity(
    tableName = "club_members",
    primaryKeys = ["clubId", "memberId"]
)
data class ClubMemberCrossRef(
    val clubId: String,
    val memberId: String,
    val role: String // Non-nullable, defaults to "member" in DB
)
