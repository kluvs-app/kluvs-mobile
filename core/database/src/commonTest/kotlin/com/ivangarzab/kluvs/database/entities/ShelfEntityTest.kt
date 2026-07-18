package com.ivangarzab.kluvs.database.entities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ShelfEntityTest {

    @Test
    fun testShelfEntity_creation() {
        val entity = ShelfEntity(
            bookId = "book-1",
            shelf = "CURRENTLY_READING",
            source = "MANUAL",
            updatedAt = "2024-02-01T12:00:00Z",
            lastFetchedAt = 1234567890L
        )

        assertEquals("book-1", entity.bookId)
        assertEquals("CURRENTLY_READING", entity.shelf)
        assertEquals("MANUAL", entity.source)
        assertEquals("2024-02-01T12:00:00Z", entity.updatedAt)
        assertEquals(1234567890L, entity.lastFetchedAt)
    }

    @Test
    fun testShelfEntity_withNullUpdatedAt() {
        val entity = ShelfEntity(
            bookId = "book-1",
            shelf = "WANT_TO_READ",
            source = "SESSION",
            updatedAt = null,
            lastFetchedAt = 1234567890L
        )

        assertEquals(null, entity.updatedAt)
    }

    @Test
    fun testShelfEntity_copy() {
        val original = ShelfEntity(
            bookId = "book-1",
            shelf = "WANT_TO_READ",
            source = "MANUAL",
            updatedAt = "2024-02-01T12:00:00Z",
            lastFetchedAt = 1234567890L
        )

        val updated = original.copy(shelf = "READ", lastFetchedAt = 9876543210L)

        assertEquals("book-1", updated.bookId)
        assertEquals("READ", updated.shelf)
        assertEquals(9876543210L, updated.lastFetchedAt)
    }

    @Test
    fun testShelfEntity_equality() {
        val entry1 = ShelfEntity("book-1", "READ", "MANUAL", null, 1234567890L)
        val entry2 = ShelfEntity("book-1", "READ", "MANUAL", null, 1234567890L)
        val entry3 = ShelfEntity("book-2", "WANT_TO_READ", "SESSION", null, 1234567890L)

        assertEquals(entry1, entry2)
        assertNotEquals(entry1, entry3)
    }
}
