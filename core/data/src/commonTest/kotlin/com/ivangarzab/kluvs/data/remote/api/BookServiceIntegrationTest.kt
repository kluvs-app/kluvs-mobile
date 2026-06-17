package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.data.remote.dtos.CreateBookRequestDto
import com.ivangarzab.kluvs.network.BuildKonfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Integration tests for [BookService] using local Supabase instance with seed data.
 *
 * Requires local Supabase running from kluvs-backend with TEST_SUPABASE_URL / TEST_SUPABASE_KEY
 * set in ~/.gradle/gradle.properties. Run via:
 *   ./gradlew :core:data:testDebugUnitTest
 *
 * Test data is defined in /kluvs-backend/supabase/seed.sql:
 *
 * Books (seeded):
 * - 1: "The Republic" by Plato (ISBN: 978-0872207363, year: -380, pages: 416)
 * - 2: "Das Kapital" by Karl Marx (ISBN: 978-0140445688, year: 1867, pages: 1152)
 * - 3: "My Birth Day" by Ivan Garza Bermea (ISBN: 978-0618260300, year: 1992, pages: 32)
 * - 4: "Nicomachean Ethics" by Aristotle (ISBN: 978-0553293357, year: -2000, pages: 368)
 * - 5: "1984" by George Orwell (ISBN: 978-0062073488, year: 1948, pages: 328)
 * - 6: "Our First Day With Her" by Skye Garza Morales (ISBN: 978-0618640157, year: 2021, pages: 24)
 * - 7: "Dune" by Frank Herbert (ISBN: 978-0441013593, year: 1965, pages: 688)
 * - 8: "The Murder of Roger Ackroyd" by Agatha Christie (ISBN: 978-0062073563, year: 1926, pages: 288)
 *
 * NOTE: search() and lookupByIsbn() call the Google Books API and require
 * GOOGLE_BOOKS_API_KEY to be configured in the backend. These are NOT tested here
 * because the local Supabase instance does not have that key configured.
 * Only register() (POST /book) is tested, as it operates entirely on the local DB.
 */
@OptIn(ExperimentalTime::class)
class BookServiceIntegrationTest {

    private lateinit var bookService: BookService

    @BeforeTest
    fun setup() {
        val url = BuildKonfig.TEST_SUPABASE_URL
        val key = BuildKonfig.TEST_SUPABASE_KEY
        println("DEBUG: TEST_SUPABASE_URL = $url")
        println("DEBUG: TEST_SUPABASE_KEY = $key")

        val supabase = createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key,
        ) {
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
                prettyPrint = true
            })

            install(Functions)
        }

        bookService = BookServiceImpl(supabase)
    }

    // =========================================================================
    // register() tests
    // =========================================================================

    @Test
    fun testRegisterNewBook() = runTest {
        // Given: a book that does not exist in the DB
        val request = CreateBookRequestDto(
            title = "Integration Test Book ${Clock.System.now().toEpochMilliseconds()}",
            author = "Test Author",
            year = 2024,
            isbn = null,
            page_count = 200,
            image_url = null,
            external_google_id = null
        )

        // When
        val response = bookService.register(request)

        // Then: should be created successfully
        assertTrue(response.success)
        assertTrue(response.created, "Expected created=true for a brand new book")
        assertNotNull(response.book)
        assertEquals(request.title, response.book.title)
        assertEquals(request.author, response.book.author)
        assertEquals(request.year, response.book.year)
        assertEquals(request.page_count, response.book.page_count)
    }

    @Test
    fun testRegisterBookWithExternalGoogleId() = runTest {
        // Given: a book with a known external_google_id (simulates Google Books import)
        val googleId = "integration-test-google-id-${Clock.System.now().toEpochMilliseconds()}"
        val request = CreateBookRequestDto(
            title = "Google Book Integration Test",
            author = "Google Author",
            year = 2023,
            isbn = null,
            page_count = 300,
            image_url = "https://example.com/cover.jpg",
            external_google_id = googleId
        )

        // When
        val response = bookService.register(request)

        // Then: first registration should create the book
        assertTrue(response.success)
        assertTrue(response.created, "Expected created=true for a new book with external_google_id")
        assertNotNull(response.book)
        assertEquals(request.title, response.book.title)
        assertEquals(request.external_google_id, response.book.external_google_id)
        assertEquals(request.image_url, response.book.image_url)
    }

    @Test
    fun testRegisterIsIdempotentViaExternalGoogleId() = runTest {
        // Given: a book already registered with a specific external_google_id
        val googleId = "idempotent-test-google-id-${Clock.System.now().toEpochMilliseconds()}"
        val request = CreateBookRequestDto(
            title = "Idempotent Book Test",
            author = "Idempotent Author",
            year = 2022,
            isbn = null,
            page_count = 150,
            image_url = null,
            external_google_id = googleId
        )

        // Register once
        val firstResponse = bookService.register(request)
        assertTrue(firstResponse.created, "First registration should create the book")
        val firstBookId = firstResponse.book.id

        // When: registering the same book again
        val secondResponse = bookService.register(request)

        // Then: should return the existing book without creating a new one
        assertTrue(secondResponse.success)
        assertFalse(secondResponse.created, "Expected created=false when book already exists")
        assertEquals(firstBookId, secondResponse.book.id, "Should return the same book ID")
        assertEquals(request.title, secondResponse.book.title)
    }
}
