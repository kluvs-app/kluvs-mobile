package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.BookDto
import com.ivangarzab.kluvs.api.models.ShelfEntryDto
import com.ivangarzab.kluvs.api.models.ShelfListResponseDto
import com.ivangarzab.kluvs.api.models.ShelfStatusDto
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
            finishedAt = null,
            source = ShelfEntryDto.Source.session,
            book = bookDto
        )

        // When: Mapping to domain
        val entry = dto.toDomain()

        // Then: All fields map over
        assertEquals(ShelfStatus.CURRENTLY_READING, entry.shelf)
        assertEquals(ShelfSource.SESSION, entry.source)
        assertNotNull(entry.updatedAt)
        assertEquals("5", entry.book.id)
        assertEquals("The Hobbit", entry.book.title)
    }

    @Test
    fun `ShelfEntryDto toDomain maps manual source`() {
        val dto = ShelfEntryDto(
            updatedAt = "2026-01-01T00:00:00Z",
            finishedAt = "2026-01-01T00:00:00Z",
            source = ShelfEntryDto.Source.manual,
            shelf = ShelfStatusDto.read,
            book = bookDto
        )

        val entry = dto.toDomain()

        assertEquals(ShelfSource.MANUAL, entry.source)
        assertEquals(ShelfStatus.READ, entry.shelf)
    }

    @Test
    fun `ShelfListResponseDto toDomain maps every entry`() {
        // Given: A response with two entries
        val response = ShelfListResponseDto(
            success = true,
            shelves = listOf(
                ShelfEntryDto(
                    updatedAt = "2026-01-01T00:00:00Z",
                    finishedAt = "2026-01-01T00:00:00Z",
                    source = ShelfEntryDto.Source.manual,
                    shelf = ShelfStatusDto.read,
                    book = bookDto
                ),
                ShelfEntryDto(
                    updatedAt = "2026-01-01T00:00:00Z",
                    finishedAt = null,
                    source = ShelfEntryDto.Source.session,
                    shelf = ShelfStatusDto.currently_reading,
                    book = bookDto
                )
            )
        )

        // When: Mapping to domain
        val entries = response.toDomain()

        // Then: Both entries map
        assertEquals(2, entries.size)
    }

    @Test
    fun `ShelfListResponseDto toDomain handles an empty shelf`() {
        assertEquals(emptyList(), ShelfListResponseDto(success = true, shelves = emptyList()).toDomain())
    }
}
