package com.ivangarzab.kluvs.database.entities

import androidx.room.Entity

/**
 * Room entity for a member's participation in a reading session.
 * Backs the reading/skipping indicators and the end-session credit count
 * so they survive cached club loads.
 */
@Entity(tableName = "session_members", primaryKeys = ["sessionId", "memberId"])
data class SessionMemberEntity(
    val sessionId: String, // References SessionEntity
    val memberId: String, // References MemberEntity
    val memberName: String?,
    val isReading: Boolean
)
