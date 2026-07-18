package com.ivangarzab.kluvs.database.entities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProgressEntityTest {

    @Test
    fun testProgressEntity_creation() {
        val entity = ProgressEntity(
            id = "progress-1",
            memberId = "member-1",
            bookId = "book-1",
            sessionId = "session-1",
            type = "PAGE",
            status = "IN_PROGRESS",
            currentPage = 42,
            percentComplete = null,
            startedAt = "2024-02-01T12:00:00Z",
            completedAt = null,
            lastFetchedAt = 1234567890L
        )

        assertEquals("progress-1", entity.id)
        assertEquals("member-1", entity.memberId)
        assertEquals("book-1", entity.bookId)
        assertEquals("session-1", entity.sessionId)
        assertEquals("PAGE", entity.type)
        assertEquals("IN_PROGRESS", entity.status)
        assertEquals(42, entity.currentPage)
        assertEquals(null, entity.percentComplete)
        assertEquals("2024-02-01T12:00:00Z", entity.startedAt)
        assertEquals(null, entity.completedAt)
        assertEquals(1234567890L, entity.lastFetchedAt)
    }

    @Test
    fun testProgressEntity_withNullFields() {
        val entity = ProgressEntity(
            id = "progress-1",
            memberId = "member-1",
            bookId = "book-1",
            sessionId = null,
            type = "PERCENT",
            status = "COMPLETED",
            currentPage = null,
            percentComplete = 100f,
            startedAt = null,
            completedAt = "2024-03-01T12:00:00Z",
            lastFetchedAt = 1234567890L
        )

        assertEquals(null, entity.sessionId)
        assertEquals(null, entity.currentPage)
        assertEquals(100f, entity.percentComplete)
    }

    @Test
    fun testProgressEntity_copy() {
        val original = ProgressEntity(
            id = "progress-1",
            memberId = "member-1",
            bookId = "book-1",
            sessionId = null,
            type = "PAGE",
            status = "IN_PROGRESS",
            currentPage = 10,
            percentComplete = null,
            startedAt = null,
            completedAt = null,
            lastFetchedAt = 1234567890L
        )

        val updated = original.copy(currentPage = 50, status = "COMPLETED")

        assertEquals("progress-1", updated.id)
        assertEquals(50, updated.currentPage)
        assertEquals("COMPLETED", updated.status)
    }

    @Test
    fun testProgressEntity_equality() {
        val progress1 = ProgressEntity(
            "progress-1", "member-1", "book-1", null, "PAGE", "IN_PROGRESS", 10, null, null, null, 1234567890L
        )
        val progress2 = ProgressEntity(
            "progress-1", "member-1", "book-1", null, "PAGE", "IN_PROGRESS", 10, null, null, null, 1234567890L
        )
        val progress3 = ProgressEntity(
            "progress-2", "member-2", "book-2", null, "PERCENT", "COMPLETED", null, 100f, null, null, 1234567890L
        )

        assertEquals(progress1, progress2)
        assertNotEquals(progress1, progress3)
    }
}
