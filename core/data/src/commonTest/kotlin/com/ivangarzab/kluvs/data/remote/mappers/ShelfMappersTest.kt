package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.BookDto
import com.ivangarzab.kluvs.api.models.GetShelf200ResponseDto
import com.ivangarzab.kluvs.api.models.ShelfEntryDto
import com.ivangarzab.kluvs.api.models.ShelfStatusDto
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ShelfMappersTest {

    private val bookDto = BookDto(
        id = 5,
        title = "The Hobbit",
        author = "J.R.R. Tolkien"
    )

    @Test
    fun `ShelfStatusDto toDomain maps all values`() {
        assertEquals(ShelfStatus.WANT_TO_READ, ShelfStatusDto.want_to_read.toDomain())
        assertEquals(ShelfStatus.CURRENTLY_READING, ShelfStatusDto.currently_reading.toDomain())
        assertEquals(ShelfStatus.READ, ShelfStatusDto.read.toDomain())
        assertEquals(ShelfStatus.NOT_FINISHED, ShelfStatusDto.not_finished.toDomain())
    }

    @Test
    fun `ShelfStatus toDto round-trips with toDomain`() {
        ShelfStatus.entries.forEach { status ->
            assertEquals(status, status.toDto().toDomain())
        }
    }

    @Test
    fun `ShelfEntryDto toDomain maps complete entry`() {
        // Given: A complete shelf entry
        val dto = ShelfEntryDto(
            shelf = ShelfStatusDto.currently_reading,
            updatedAt = "2026-07-01T10:30:00",
            source = ShelfEntryDto.Source.session,
            book = bookDto
        )

        // When: Mapping to domain
        val entry = dto.toDomain()

        // Then: All fields map over
        assertNotNull(entry)
        assertEquals(ShelfStatus.CURRENTLY_READING, entry.shelf)
        assertEquals(ShelfSource.SESSION, entry.source)
        assertNotNull(entry.updatedAt)
        assertEquals("5", entry.book.id)
        assertEquals("The Hobbit", entry.book.title)
    }

    @Test
    fun `ShelfEntryDto toDomain defaults missing source to MANUAL`() {
        val dto = ShelfEntryDto(shelf = ShelfStatusDto.read, book = bookDto)

        val entry = dto.toDomain()

        assertNotNull(entry)
        assertEquals(ShelfSource.MANUAL, entry.source)
        assertNull(entry.updatedAt)
    }

    @Test
    fun `ShelfEntryDto toDomain returns null when shelf or book is missing`() {
        assertNull(ShelfEntryDto(shelf = null, book = bookDto).toDomain())
        assertNull(ShelfEntryDto(shelf = ShelfStatusDto.read, book = null).toDomain())
    }

    @Test
    fun `GetShelf200ResponseDto toDomain skips malformed entries`() {
        // Given: A response with one valid and one bookless entry
        val response = GetShelf200ResponseDto(
            success = true,
            shelves = listOf(
                ShelfEntryDto(shelf = ShelfStatusDto.read, book = bookDto),
                ShelfEntryDto(shelf = ShelfStatusDto.read, book = null)
            )
        )

        // When: Mapping to domain
        val entries = response.toDomain()

        // Then: Only the valid entry survives
        assertEquals(1, entries.size)
    }

    @Test
    fun `GetShelf200ResponseDto toDomain handles null shelves list`() {
        assertEquals(emptyList(), GetShelf200ResponseDto(success = true).toDomain())
    }
}
