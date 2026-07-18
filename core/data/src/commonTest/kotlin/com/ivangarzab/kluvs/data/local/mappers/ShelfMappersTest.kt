package com.ivangarzab.kluvs.data.local.mappers

import com.ivangarzab.kluvs.database.entities.ShelfEntity
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ShelfMappersTest {

    private val book = Book(id = "book-1", title = "The Hobbit", author = "J.R.R. Tolkien", isbn = null)

    @Test
    fun testShelfEntity_toDomain() {
        val entity = ShelfEntity(
            bookId = "book-1",
            shelf = "CURRENTLY_READING",
            source = "SESSION",
            updatedAt = "2024-02-01T12:00:00",
            lastFetchedAt = 1234567890L
        )

        val domain = entity.toDomain(book)

        assertEquals(ShelfStatus.CURRENTLY_READING, domain.shelf)
        assertEquals(ShelfSource.SESSION, domain.source)
        assertEquals(book, domain.book)
        assertEquals(LocalDateTime.parse("2024-02-01T12:00:00"), domain.updatedAt)
    }

    @Test
    fun testShelfEntry_toEntity() {
        val domain = ShelfEntry(
            shelf = ShelfStatus.READ,
            source = ShelfSource.MANUAL,
            updatedAt = LocalDateTime.parse("2024-02-01T12:00:00"),
            book = book
        )

        val entity = domain.toEntity()

        assertEquals("book-1", entity.bookId)
        assertEquals("READ", entity.shelf)
        assertEquals("MANUAL", entity.source)
        assertEquals("2024-02-01T12:00", entity.updatedAt)
        assertNotNull(entity.lastFetchedAt)
    }
}
