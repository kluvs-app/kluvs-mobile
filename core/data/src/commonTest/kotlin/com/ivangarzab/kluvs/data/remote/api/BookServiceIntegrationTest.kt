package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.BookRegistrationRequestDto
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
 * Books (seeded — all sourced from Google Books with external_google_id set):
 * - 1: "The Republic" by Plato (ISBN: 978-0140455113, year: -380, pages: 416)
 * - 2: "Nicomachean Ethics" by Aristotle (ISBN: 978-0199213610, year: -350, pages: 304)
 * - 3: "Capital" by Karl Marx (ISBN: 978-0140445688, year: 1867, pages: 1152)
 * - 4: "1984" by George Orwell (ISBN: 978-0451524935, year: 1949, pages: 328)
 * - 5: "Brave New World" by Aldous Huxley (ISBN: 978-0060850524, year: 1932, pages: 288)
 * - 6: "The Prospect of a Humanitarian Artificial Intelligence" by Carlos Montemayor (ISBN: 978-1350348400, year: 2021, pages: 296)
 * - 7: "Ficciones" by Jorge Luis Borges (ISBN: 978-8499089183, year: 1944, pages: 224)
 * - 8: "Diarios de Motocicleta" by Ernesto "Che" Guevara (ISBN: 978-8408068523, year: 1993, pages: 163)
 * - 9: "Meditations" by Marcus Aurelius (ISBN: 978-0140449334, year: 180, pages: 304)
 *
 * These tests create their own books via register() and do not depend on the seeded rows.
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
        val request = BookRegistrationRequestDto(
            title = "Integration Test Book ${Clock.System.now().toEpochMilliseconds()}",
            author = "Test Author",
            year = 2024,
            isbn = null,
            pageCount = 200,
            imageUrl = null,
            externalGoogleId = null
        )

        // When
        val response = bookService.register(request)

        // Then: should be created successfully
        assertTrue(response.success == true)
        assertTrue(response.created == true, "Expected created=true for a brand new book")
        val book = assertNotNull(response.book)
        assertEquals(request.title, book.title)
        assertEquals(request.author, book.author)
        assertEquals(request.year, book.year)
        assertEquals(request.pageCount, book.pageCount)
    }

    @Test
    fun testRegisterBookWithExternalGoogleId() = runTest {
        // Given: a book with a known external_google_id (simulates Google Books import)
        val googleId = "integration-test-google-id-${Clock.System.now().toEpochMilliseconds()}"
        val request = BookRegistrationRequestDto(
            title = "Google Book Integration Test",
            author = "Google Author",
            year = 2023,
            isbn = null,
            pageCount = 300,
            imageUrl = "https://example.com/cover.jpg",
            externalGoogleId = googleId
        )

        // When
        val response = bookService.register(request)

        // Then: first registration should create the book
        assertTrue(response.success == true)
        assertTrue(response.created == true, "Expected created=true for a new book with external_google_id")
        val book = assertNotNull(response.book)
        assertEquals(request.title, book.title)
        assertEquals(request.externalGoogleId, book.externalGoogleId)
        assertEquals(request.imageUrl, book.imageUrl)
    }

    @Test
    fun testRegisterIsIdempotentViaExternalGoogleId() = runTest {
        // Given: a book already registered with a specific external_google_id
        val googleId = "idempotent-test-google-id-${Clock.System.now().toEpochMilliseconds()}"
        val request = BookRegistrationRequestDto(
            title = "Idempotent Book Test",
            author = "Idempotent Author",
            year = 2022,
            isbn = null,
            pageCount = 150,
            imageUrl = null,
            externalGoogleId = googleId
        )

        // Register once
        val firstResponse = bookService.register(request)
        assertTrue(firstResponse.created == true, "First registration should create the book")
        val firstBookId = assertNotNull(firstResponse.book).id

        // When: registering the same book again
        val secondResponse = bookService.register(request)

        // Then: should return the existing book without creating a new one
        assertTrue(secondResponse.success == true)
        assertFalse(secondResponse.created == true, "Expected created=false when book already exists")
        val secondBook = assertNotNull(secondResponse.book)
        assertEquals(firstBookId, secondBook.id, "Should return the same book ID")
        assertEquals(request.title, secondBook.title)
    }
}
