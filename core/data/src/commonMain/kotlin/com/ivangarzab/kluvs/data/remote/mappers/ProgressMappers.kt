package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.BookSummaryDto
import com.ivangarzab.kluvs.api.models.ProgressCreateRequestDto
import com.ivangarzab.kluvs.api.models.ProgressUpdateRequestDto
import com.ivangarzab.kluvs.api.models.ReadingProgressDto
import com.ivangarzab.kluvs.model.BookSummary
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.ReadingProgress

/**
 * Maps a [com.ivangarzab.kluvs.api.models.ReadingProgressDto] from the API to a
 * [ReadingProgress] domain model.
 *
 * Returns null when the progress type is unknown to this app version: reading a page
 * number as a percentage (or the reverse) would show wrong progress, so the entry is dropped.
 */
fun ReadingProgressDto.toDomain(): ReadingProgress? {
    val type = progressType.toDomain() ?: run {
        Bark.w("Unknown progress type from API; dropping progress entry (ID: $id)")
        return null
    }
    return ReadingProgress(
        id = id,
        memberId = memberId.toString(),
        bookId = bookId.toString(),
        sessionId = sessionId,
        type = type,
        status = status.toDomain(id),
        currentPage = currentPage,
        percentComplete = percentComplete,
        startedAt = startedAt.parseDateStringOrNull(),
        completedAt = completedAt.parseDateStringOrNull(),
        book = book?.toDomain()
    )
}

/**
 * Maps a [com.ivangarzab.kluvs.api.models.BookSummaryDto] to a [BookSummary] domain model.
 */
fun BookSummaryDto.toDomain(): BookSummary = BookSummary(
    id = id.toString(),
    title = title,
    pageCount = pageCount,
    imageUrl = imageUrl
)

/** Returns null for a progress type this app version doesn't know. */
fun ReadingProgressDto.ProgressType.toDomain(): ProgressType? = when (this) {
    ReadingProgressDto.ProgressType.page -> ProgressType.PAGE
    ReadingProgressDto.ProgressType.percent -> ProgressType.PERCENT
    ReadingProgressDto.ProgressType.unknown_default_open_api -> null
}

/** Unknown statuses fall back to [ProgressStatus.IN_PROGRESS], which claims no completion. */
fun ReadingProgressDto.Status.toDomain(progressId: String): ProgressStatus = when (this) {
    ReadingProgressDto.Status.in_progress -> ProgressStatus.IN_PROGRESS
    ReadingProgressDto.Status.completed -> ProgressStatus.COMPLETED
    ReadingProgressDto.Status.unknown_default_open_api -> {
        Bark.w("Unknown progress status from API; falling back to IN_PROGRESS (ID: $progressId)")
        ProgressStatus.IN_PROGRESS
    }
}

fun ProgressType.toCreateDto(): ProgressCreateRequestDto.ProgressType = when (this) {
    ProgressType.PAGE -> ProgressCreateRequestDto.ProgressType.page
    ProgressType.PERCENT -> ProgressCreateRequestDto.ProgressType.percent
}

fun ProgressType.toUpdateDto(): ProgressUpdateRequestDto.ProgressType = when (this) {
    ProgressType.PAGE -> ProgressUpdateRequestDto.ProgressType.page
    ProgressType.PERCENT -> ProgressUpdateRequestDto.ProgressType.percent
}

fun ProgressStatus.toUpdateDto(): ProgressUpdateRequestDto.Status = when (this) {
    ProgressStatus.IN_PROGRESS -> ProgressUpdateRequestDto.Status.in_progress
    ProgressStatus.COMPLETED -> ProgressUpdateRequestDto.Status.completed
}

/**
 * Maps a [ProgressStatus] to the raw query-parameter value the progress endpoint expects.
 */
fun ProgressStatus.toQueryValue(): String = when (this) {
    ProgressStatus.IN_PROGRESS -> "in_progress"
    ProgressStatus.COMPLETED -> "completed"
}
