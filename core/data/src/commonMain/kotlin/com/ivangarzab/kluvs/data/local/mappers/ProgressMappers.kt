package com.ivangarzab.kluvs.data.local.mappers

import com.ivangarzab.kluvs.data.remote.mappers.parseDateStringOrNull
import com.ivangarzab.kluvs.database.entities.ProgressEntity
import com.ivangarzab.kluvs.model.BookSummary
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.ReadingProgress
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Maps a [ProgressEntity] from the local database to a [ReadingProgress] domain model.
 * The associated [BookSummary], if available, must be loaded separately.
 */
fun ProgressEntity.toDomain(book: BookSummary?): ReadingProgress {
    return ReadingProgress(
        id = id,
        memberId = memberId,
        bookId = bookId,
        sessionId = sessionId,
        type = ProgressType.valueOf(type),
        status = ProgressStatus.valueOf(status),
        currentPage = currentPage,
        percentComplete = percentComplete,
        startedAt = startedAt.parseDateStringOrNull(),
        completedAt = completedAt.parseDateStringOrNull(),
        book = book
    )
}

/**
 * Maps a [ReadingProgress] domain model to a [ProgressEntity] for local database storage.
 * Sets lastFetchedAt to current time.
 */
@OptIn(ExperimentalTime::class)
fun ReadingProgress.toEntity(): ProgressEntity {
    return ProgressEntity(
        id = id,
        memberId = memberId,
        bookId = bookId,
        sessionId = sessionId,
        type = type.name,
        status = status.name,
        currentPage = currentPage,
        percentComplete = percentComplete,
        startedAt = startedAt?.toString(),
        completedAt = completedAt?.toString(),
        lastFetchedAt = Clock.System.now().toEpochMilliseconds()
    )
}
