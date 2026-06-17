package com.ivangarzab.kluvs.data.local.mappers

import com.ivangarzab.kluvs.data.remote.mappers.parseDateString
import com.ivangarzab.kluvs.database.entities.MemberEntity
import com.ivangarzab.kluvs.model.Member
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Maps a [MemberEntity] from the local database to a [Member] domain model.
 * Note: clubs and shameClubs relationships are not loaded from the entity (left as null).
 */
fun MemberEntity.toDomain(): Member {
    return Member(
        id = id,
        name = name ?: "",
        handle = handle,
        avatarPath = avatarPath,
        booksRead = booksRead,
        userId = userId,
        createdAt = createdAt?.parseDateString(),
        clubs = null, // Relationship not stored in entity
        shameClubs = null // Relationship not stored in entity
    )
}

/**
 * Maps a [Member] domain model to a [MemberEntity] for local database storage.
 * Sets lastFetchedAt to current time.
 */
@OptIn(ExperimentalTime::class)
fun Member.toEntity(): MemberEntity {
    return MemberEntity(
        id = id,
        userId = userId,
        name = name,
        handle = handle,
        avatarPath = avatarPath,
        booksRead = booksRead,
        createdAt = createdAt?.toString(),
        lastFetchedAt = Clock.System.now().toEpochMilliseconds()
    )
}
