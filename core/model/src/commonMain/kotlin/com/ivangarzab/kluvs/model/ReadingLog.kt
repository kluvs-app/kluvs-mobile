package com.ivangarzab.kluvs.model

import kotlinx.datetime.LocalDateTime

/**
 * Domain model for the member's reading log: every session they are part of,
 * grouped by whether the session is still active or already finished.
 */
data class ReadingLog(

    val active: List<ReadingLogEntry>,

    val finished: List<ReadingLogEntry>
)

/**
 * A single session entry in the member's [ReadingLog].
 */
data class ReadingLogEntry(

    val sessionId: String,

    val dueDate: LocalDateTime? = null,

    val book: BookSummary? = null,

    val club: ClubPreview? = null
)
