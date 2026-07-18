package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.BookSummaryDto
import com.ivangarzab.kluvs.api.models.ProgressCreateRequestDto
import com.ivangarzab.kluvs.api.models.ProgressUpdateRequestDto
import com.ivangarzab.kluvs.api.models.ReadingProgressDto
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProgressMappersTest {

    @Test
    fun `ReadingProgressDto toDomain maps complete entry`() {
        // Given: A complete page-based progress entry
        val dto = ReadingProgressDto(
            id = "progress-1",
            memberId = 1,
            bookId = 5,
            progressType = ReadingProgressDto.ProgressType.page,
            status = ReadingProgressDto.Status.in_progress,
            startedAt = "2026-07-01T10:30:00",
            updatedAt = "2026-07-02T11:00:00",
            sessionId = "session-1",
            currentPage = 50,
            book = BookSummaryDto(id = 5, title = "The Hobbit", pageCount = 310)
        )

        // When: Mapping to domain
        val progress = dto.toDomain()

        // Then: All fields map over, with IDs stringified
        assertEquals("progress-1", progress.id)
        assertEquals("1", progress.memberId)
        assertEquals("5", progress.bookId)
        assertEquals("session-1", progress.sessionId)
        assertEquals(ProgressType.PAGE, progress.type)
        assertEquals(ProgressStatus.IN_PROGRESS, progress.status)
        assertEquals(50, progress.currentPage)
        assertNotNull(progress.startedAt)
        assertNull(progress.completedAt)
        assertEquals("The Hobbit", progress.book?.title)
        assertEquals(310, progress.book?.pageCount)
    }

    @Test
    fun `ReadingProgressDto toDomain maps completed percent entry`() {
        val dto = ReadingProgressDto(
            id = "progress-2",
            memberId = 1,
            bookId = 7,
            progressType = ReadingProgressDto.ProgressType.percent,
            status = ReadingProgressDto.Status.completed,
            startedAt = "2026-07-01T10:30:00",
            updatedAt = "2026-07-05T09:00:00",
            percentComplete = 100f,
            completedAt = "2026-07-05T09:00:00"
        )

        val progress = dto.toDomain()

        assertEquals(ProgressType.PERCENT, progress.type)
        assertEquals(ProgressStatus.COMPLETED, progress.status)
        assertEquals(100f, progress.percentComplete)
        assertNotNull(progress.completedAt)
        assertNull(progress.book)
    }

    @Test
    fun `ProgressType maps to create and update request enums`() {
        assertEquals(ProgressCreateRequestDto.ProgressType.page, ProgressType.PAGE.toCreateDto())
        assertEquals(ProgressCreateRequestDto.ProgressType.percent, ProgressType.PERCENT.toCreateDto())
        assertEquals(ProgressUpdateRequestDto.ProgressType.page, ProgressType.PAGE.toUpdateDto())
        assertEquals(ProgressUpdateRequestDto.ProgressType.percent, ProgressType.PERCENT.toUpdateDto())
    }

    @Test
    fun `ProgressStatus maps to update request enum and query value`() {
        assertEquals(ProgressUpdateRequestDto.Status.in_progress, ProgressStatus.IN_PROGRESS.toUpdateDto())
        assertEquals(ProgressUpdateRequestDto.Status.completed, ProgressStatus.COMPLETED.toUpdateDto())
        assertEquals("in_progress", ProgressStatus.IN_PROGRESS.toQueryValue())
        assertEquals("completed", ProgressStatus.COMPLETED.toQueryValue())
    }

    @Test
    fun `BookSummaryDto toDomain falls back to empty strings for missing id and title`() {
        val summary = BookSummaryDto().toDomain()

        assertEquals("", summary.id)
        assertEquals("", summary.title)
    }
}
