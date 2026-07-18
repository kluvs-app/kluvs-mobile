package com.ivangarzab.kluvs.model

import kotlinx.datetime.LocalDateTime

/**
 * Domain model for the member's reading progress on a book.
 */
data class ReadingProgress(

    val id: String,

    val memberId: String,

    val bookId: String,

    /** Session this progress is tied to, if any. **/
    val sessionId: String? = null,

    val type: ProgressType,

    val status: ProgressStatus,

    /** Current page, populated when [type] is [ProgressType.PAGE]. **/
    val currentPage: Int? = null,

    /** Percent complete (0-100), populated when [type] is [ProgressType.PERCENT]. **/
    val percentComplete: Float? = null,

    val startedAt: LocalDateTime? = null,

    val completedAt: LocalDateTime? = null,

    val book: BookSummary? = null
)

/**
 * How progress on a book is being measured.
 */
enum class ProgressType {
    PAGE,
    PERCENT
}

/**
 * Whether the member is still reading the book or has finished it.
 */
enum class ProgressStatus {
    IN_PROGRESS,
    COMPLETED
}
