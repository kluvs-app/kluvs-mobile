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
 * to a [ReadingLog] domain model.
 */
fun SessionReadingLogResponseDto.toDomain(): ReadingLog {
    return ReadingLog(
        active = readingLog.active.map { it.toDomain() },
        finished = readingLog.finished.map { it.toDomain() }
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.api.models.SessionReadingLogEntryDto] to a [ReadingLogEntry]
 * domain model.
 */
fun SessionReadingLogEntryDto.toDomain(): ReadingLogEntry {
    return ReadingLogEntry(
        sessionId = id,
        dueDate = dueDate.parseDateStringOrNull(),
        book = book.toDomain(),
        club = club.toDomain()
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.api.models.SessionReadingLogBookDto] to a [BookSummary]
 * domain model.
 */
fun SessionReadingLogBookDto.toDomain(): BookSummary {
    return BookSummary(
        id = id.toString(),
        title = title,
        author = author,
        imageUrl = imageUrl
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.api.models.SessionReadingLogClubDto] to a [ClubPreview]
 * domain model.
 */
fun SessionReadingLogClubDto.toDomain(): ClubPreview {
    return ClubPreview(
        id = id,
        name = name
    )
}
