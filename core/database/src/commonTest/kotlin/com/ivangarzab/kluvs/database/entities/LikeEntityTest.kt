package com.ivangarzab.kluvs.database.entities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LikeEntityTest {

    @Test
    fun testLikeEntity_creation() {
        val entity = LikeEntity(bookId = "book-1", liked = true, lastFetchedAt = 1234567890L)

        assertEquals("book-1", entity.bookId)
        assertEquals(true, entity.liked)
        assertEquals(1234567890L, entity.lastFetchedAt)
    }

    @Test
    fun testLikeEntity_copy() {
        val original = LikeEntity(bookId = "book-1", liked = true, lastFetchedAt = 1234567890L)
        val updated = original.copy(liked = false, lastFetchedAt = 9876543210L)

        assertEquals("book-1", updated.bookId)
        assertEquals(false, updated.liked)
        assertEquals(9876543210L, updated.lastFetchedAt)
    }

    @Test
    fun testLikeEntity_equality() {
        val like1 = LikeEntity("book-1", true, 1234567890L)
        val like2 = LikeEntity("book-1", true, 1234567890L)
        val like3 = LikeEntity("book-2", false, 1234567890L)

        assertEquals(like1, like2)
        assertNotEquals(like1, like3)
    }
}
