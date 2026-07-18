package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.SessionReadingLogBookDto
import com.ivangarzab.kluvs.api.models.SessionReadingLogClubDto
import com.ivangarzab.kluvs.api.models.SessionReadingLogEntryDto
import com.ivangarzab.kluvs.api.models.SessionReadingLogResponseDto
import com.ivangarzab.kluvs.model.BookSummary
import com.ivangarzab.kluvs.model.ClubPreview
import com.ivangarzab.kluvs.model.ReadingLog
import com.ivangarzab.kluvs.model.ReadingLogEntry

/**
 * Maps a [com.ivangarzab.kluvs.api.models.SessionReadingLogResponseDto] from the API
 * to a [ReadingLog] domain model, skipping malformed entries.
 */
fun SessionReadingLogResponseDto.toDomain(): ReadingLog {
    return ReadingLog(
        active = readingLog?.active.orEmpty().mapNotNull { it.toDomain() },
        finished = readingLog?.finished.orEmpty().mapNotNull { it.toDomain() }
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.api.models.SessionReadingLogEntryDto] to a [ReadingLogEntry]
 * domain model, or null when the entry carries no session ID.
 */
fun SessionReadingLogEntryDto.toDomain(): ReadingLogEntry? {
    val sessionId = id ?: return null
    return ReadingLogEntry(
        sessionId = sessionId,
        dueDate = dueDate.parseDateStringOrNull(),
        book = book?.toDomain(),
        club = club?.toDomain()
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.api.models.SessionReadingLogBookDto] to a [BookSummary]
 * domain model, or null when the book carries no ID.
 */
fun SessionReadingLogBookDto.toDomain(): BookSummary? {
    val bookId = id ?: return null
    return BookSummary(
        id = bookId.toString(),
        title = title ?: "",
        author = author,
        imageUrl = imageUrl
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.api.models.SessionReadingLogClubDto] to a [ClubPreview]
 * domain model, or null when the club carries no ID.
 */
fun SessionReadingLogClubDto.toDomain(): ClubPreview? {
    val clubId = id ?: return null
    return ClubPreview(
        id = clubId,
        name = name ?: ""
    )
}
