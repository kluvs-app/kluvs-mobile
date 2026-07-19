package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.BookDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BookMappersTest {

    @Test
    fun `toDomain maps all fields correctly`() {
        // Given: A BookDto with all fields populated
        val dto = BookDto(
            id = 123,
            title = "The Hobbit",
            author = "J.R.R. Tolkien",
            edition = "First Edition",
            year = 1937,
            isbn = "978-0-395-07122-1",
            pageCount = 310,
            imageUrl = "https://example.com/hobbit.jpg",
            externalGoogleId = "goog-123"
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: All fields are mapped correctly
        assertEquals("123", domain.id)
        assertEquals("The Hobbit", domain.title)
        assertEquals("J.R.R. Tolkien", domain.author)
        assertEquals("First Edition", domain.edition)
        assertEquals(1937, domain.year)
        assertEquals("978-0-395-07122-1", domain.isbn)
        assertEquals(310, domain.pageCount)
        assertEquals("https://example.com/hobbit.jpg", domain.imageUrl)
        assertEquals("goog-123", domain.externalGoogleId)
    }

    @Test
    fun `toDomain handles nullable fields correctly`() {
        // Given: A BookDto with only required fields
        val dto = BookDto(
            id = 0,
            title = "Some Book",
            author = "Some Author",
            edition = null,
            year = null,
            isbn = null,
            pageCount = null
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: Nullable fields are null
        assertEquals("0", domain.id)
        assertEquals("Some Book", domain.title)
        assertEquals("Some Author", domain.author)
        assertNull(domain.edition)
        assertNull(domain.year)
        assertNull(domain.isbn)
        assertNull(domain.pageCount)
        assertNull(domain.imageUrl)
        assertNull(domain.externalGoogleId)
    }

    @Test
    fun `toDomain falls back to externalGoogleId when id is absent`() {
        // Given: A BookDto as returned by a Google Books search (no local DB id yet)
        val dto = BookDto(
            id = null,
            title = "Some Book",
            author = "Some Author",
            externalGoogleId = "goog-456"
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: The Google volume id is used as a stable fallback id
        assertEquals("goog-456", domain.id)
    }
}
