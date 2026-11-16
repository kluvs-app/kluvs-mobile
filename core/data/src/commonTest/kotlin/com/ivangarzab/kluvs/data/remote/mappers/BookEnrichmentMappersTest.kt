package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.AuthorInfoDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BookEnrichmentMappersTest {

    @Test
    fun `AuthorInfoDto toDomain maps all fields including description`() {
        // Given: An AuthorInfoDto with all fields populated
        val dto = AuthorInfoDto(
            name = "F. Scott Fitzgerald",
            description = "American novelist",
            imageUrl = "https://example.com/fitzgerald.jpg",
            extract = "Francis Scott Key Fitzgerald was an American novelist."
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: All fields are mapped, extract becomes bio
        assertEquals("F. Scott Fitzgerald", domain.name)
        assertEquals("American novelist", domain.description)
        assertEquals("https://example.com/fitzgerald.jpg", domain.imageUrl)
        assertEquals("Francis Scott Key Fitzgerald was an American novelist.", domain.bio)
    }

    @Test
    fun `AuthorInfoDto toDomain maps null description to null`() {
        // Given: An AuthorInfoDto without a description
        val dto = AuthorInfoDto(
            name = "F. Scott Fitzgerald",
            description = null,
            imageUrl = null,
            extract = "Francis Scott Key Fitzgerald was an American novelist."
        )

        // When: Mapping to domain
        val domain = dto.toDomain()

        // Then: description stays null, other fields are unaffected
        assertNull(domain.description)
        assertEquals("F. Scott Fitzgerald", domain.name)
        assertEquals("Francis Scott Key Fitzgerald was an American novelist.", domain.bio)
    }
}
