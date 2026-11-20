package com.ivangarzab.kluvs.data.local.mappers

import com.ivangarzab.kluvs.model.Author
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.BookEnrichment
import com.ivangarzab.kluvs.model.BookVolumeInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BookEnrichmentMappersTest {

    private val full = BookEnrichment(
        volumeInfo = BookVolumeInfo(
            subtitle = "There and Back Again",
            publisher = "Allen & Unwin",
            description = "A hobbit's \"journey\"\nwith newline",
            categories = listOf("Fiction", "Fantasy"),
            language = "en",
            isbn13 = "9780261102217",
            isbn10 = "0261102214"
        ),
        author = Author(
            name = "J.R.R. Tolkien",
            description = "English writer and philologist",
            imageUrl = "https://img/tolkien.jpg",
            bio = "John Ronald Reuel Tolkien was..."
        ),
        authorBooks = listOf(
            Book(
                id = "goog-lotr",
                title = "The Fellowship of the Ring",
                author = "J.R.R. Tolkien",
                isbn = null,
                year = 1954,
                imageUrl = "https://img/lotr.jpg",
                externalGoogleId = "goog-lotr"
            ),
            Book(id = "goog-silm", title = "The Silmarillion", author = "J.R.R. Tolkien", isbn = null)
        )
    )

    @Test
    fun `round trip preserves every field including author description and author books`() {
        val restored = full.toEntity("goog-hobbit|J.R.R. Tolkien").toDomain()

        assertEquals(full, restored)
        assertEquals("English writer and philologist", restored.author?.description)
        assertEquals(2, restored.authorBooks.size)
    }

    @Test
    fun `round trip preserves null volume info and author with empty author books`() {
        val empty = BookEnrichment(volumeInfo = null, author = null, authorBooks = emptyList())

        val entity = empty.toEntity("key")

        assertNull(entity.volumeInfoJson)
        assertNull(entity.authorJson)
        assertEquals(empty, entity.toDomain())
    }

    @Test
    fun `toEntity stores key and sets lastFetchedAt`() {
        val entity = full.toEntity("my-key")

        assertEquals("my-key", entity.cacheKey)
        assertTrue(entity.lastFetchedAt > 0)
    }

    @Test
    fun `cache key distinguishes author-only and id-only requests`() {
        assertEquals("id|author", bookEnrichmentCacheKey("id", "author"))
        assertEquals("|author", bookEnrichmentCacheKey(null, "author"))
        assertEquals("id|", bookEnrichmentCacheKey("id", null))
    }
}
