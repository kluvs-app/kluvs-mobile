package com.ivangarzab.kluvs.data.local.mappers

import com.ivangarzab.kluvs.database.entities.ProgressEntity
import com.ivangarzab.kluvs.model.BookSummary
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.ReadingProgress
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProgressMappersTest {

    @Test
    fun testProgressEntity_toDomain() {
        val entity = ProgressEntity(
            id = "progress-1",
            memberId = "member-1",
            bookId = "book-1",
            sessionId = "session-1",
            type = "PAGE",
            status = "IN_PROGRESS",
            currentPage = 42,
            percentComplete = null,
            startedAt = "2024-02-01T12:00:00",
            completedAt = null,
            lastFetchedAt = 1234567890L
        )
        val book = BookSummary(id = "book-1", title = "The Hobbit", author = "J.R.R. Tolkien")

        val domain = entity.toDomain(book)

        assertEquals("progress-1", domain.id)
        assertEquals("member-1", domain.memberId)
        assertEquals("book-1", domain.bookId)
        assertEquals("session-1", domain.sessionId)
        assertEquals(ProgressType.PAGE, domain.type)
        assertEquals(ProgressStatus.IN_PROGRESS, domain.status)
        assertEquals(42, domain.currentPage)
        assertEquals(LocalDateTime.parse("2024-02-01T12:00:00"), domain.startedAt)
        assertNull(domain.completedAt)
        assertEquals(book, domain.book)
    }

    @Test
    fun testReadingProgress_toEntity() {
        val domain = ReadingProgress(
            id = "progress-1",
            memberId = "member-1",
            bookId = "book-1",
            sessionId = null,
            type = ProgressType.PERCENT,
            status = ProgressStatus.COMPLETED,
            percentComplete = 100f,
            completedAt = LocalDateTime.parse("2024-03-01T12:00:00")
        )

        val entity = domain.toEntity()

        assertEquals("progress-1", entity.id)
        assertEquals("member-1", entity.memberId)
        assertEquals("book-1", entity.bookId)
        assertEquals("PERCENT", entity.type)
        assertEquals("COMPLETED", entity.status)
        assertEquals(100f, entity.percentComplete)
        assertEquals("2024-03-01T12:00", entity.completedAt)
        assertNotNull(entity.lastFetchedAt)
    }
}
