package com.ivangarzab.kluvs.data.local.mappers

import com.ivangarzab.kluvs.database.entities.LikeEntity
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Maps a [Boolean] liked state to a [LikeEntity] for local database storage.
 * Sets lastFetchedAt to current time.
 */
@OptIn(ExperimentalTime::class)
fun Boolean.toEntity(bookId: String): LikeEntity {
    return LikeEntity(
        bookId = bookId,
        liked = this,
        lastFetchedAt = Clock.System.now().toEpochMilliseconds()
    )
}
