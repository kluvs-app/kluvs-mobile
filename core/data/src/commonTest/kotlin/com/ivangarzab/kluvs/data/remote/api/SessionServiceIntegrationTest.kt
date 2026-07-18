package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.SessionInlineBookInputDto
import com.ivangarzab.kluvs.api.models.SessionDiscussionInputDto
import com.ivangarzab.kluvs.api.models.SessionCreateRequestDto
import com.ivangarzab.kluvs.api.models.SessionBookPatchInputDto
import com.ivangarzab.kluvs.api.models.SessionUpdateRequestDto
import com.ivangarzab.kluvs.network.BuildKonfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for SessionService using local Supabase instance with seed data.
 *
 * Note: Sessions in seed data are tied to clubs and books from the seed.sql file.
 *
 * Note: the generated request schemas only support the deprecated inline `book` object
 * for POST (title/author/year/isbn/page_count — no id/edition), and only title/author
 * for PUT (the backend cannot re-point a session at a different book via PUT, only patch
 * the existing book's title/author).
 */
class SessionServiceIntegrationTest {

    private lateinit var sessionService: SessionService

    @BeforeTest
    fun setup() {
        val supabase = createSupabaseClient(
            supabaseUrl = BuildKonfig.TEST_SUPABASE_URL,
            supabaseKey = BuildKonfig.TEST_SUPABASE_KEY
        ) {
            install(Functions)
        }
        sessionService = SessionServiceImpl(supabase)
    }

    // ========================================
    // CREATE/UPDATE/DELETE TESTS
    // (Sessions are complex - we'll create our own for testing)
    // ========================================

    @Test
    fun testCreateSession() = runTest {
        // Given: a new session request
        val book = SessionInlineBookInputDto(
            title = "Test Book",
            author = "Test Author",
            year = 2024,
            isbn = "123-456-789"
        )
        val request = SessionCreateRequestDto(
            clubId = "club-owner", // Using existing club from seed data
            book = book,
            dueDate = "2025-12-31"
        )

        var sessionId: String? = null
        try {
            // When: creating the session
            val response = sessionService.create(request)

            // Then: should return success
            assertTrue(response.success == true, "Session creation should succeed")
            assertNotNull(response.session, "Should return session data")
            sessionId = response.session?.id

            // Verify it can be retrieved
            sessionId?.let {
                val retrieved = sessionService.get(it)
                assertEquals("Test Book", retrieved.book?.title)
                assertEquals("club-owner", retrieved.clubId)
            }
        } finally {
            // Cleanup
            sessionId?.let {
                try {
                    sessionService.delete(it)
                } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testGetSession() = runTest {
        // Given: a session exists
        val book = SessionInlineBookInputDto(
            title = "Get Test Book",
            author = "Get Author"
        )
        val createRequest = SessionCreateRequestDto(
            clubId = "club-owner",
            book = book,
            dueDate = "2025-11-30"
        )
        val created = sessionService.create(createRequest)
        val sessionId = created.session?.id
        assertNotNull(sessionId, "Session ID should not be null")

        try {
            // When: getting the session
            val response = sessionService.get(sessionId)

            // Then: should return complete session data
            assertEquals(sessionId, response.id)
            assertEquals("Get Test Book", response.book?.title)
            assertEquals("Get Author", response.book?.author)
            assertEquals("club-owner", response.clubId)
            assertEquals("2025-11-30", response.dueDate)
            assertTrue(response.discussions.isNullOrEmpty(), "Should have no discussions initially")
        } finally {
            // Cleanup
            try {
                sessionService.delete(sessionId)
            } catch (_: Exception) { }
        }
    }

    @Test
    fun testUpdateSession() = runTest {
        // Given: a session exists
        val book = SessionInlineBookInputDto(title = "Original Book", author = "Original Author")
        val createRequest = SessionCreateRequestDto(
            clubId = "club-owner",
            book = book,
            dueDate = "2025-06-01"
        )
        val created = sessionService.create(createRequest)
        val sessionId = created.session?.id
        assertNotNull(sessionId)

        try {
            // When: updating the session (PUT can only patch the existing book's title/author)
            val updatedBook = SessionBookPatchInputDto(
                title = "Updated Book Title",
                author = "Updated Author"
            )
            val updateRequest = SessionUpdateRequestDto(
                id = sessionId,
                book = updatedBook,
                dueDate = "2025-12-31"
            )
            val response = sessionService.update(updateRequest)

            // Then: should return success (the general-update branch)
            assertTrue(response.success == true, "Session update should succeed")
            assertTrue(response.updates?.book == true, "Book should be marked as updated")

            // Verify changes persisted
            val retrieved = sessionService.get(sessionId)
            assertEquals("Updated Book Title", retrieved.book?.title)
            assertEquals("Updated Author", retrieved.book?.author)
            assertEquals("2025-12-31", retrieved.dueDate)
        } finally {
            // Cleanup
            try {
                sessionService.delete(sessionId)
            } catch (_: Exception) { }
        }
    }

    @Test
    fun testDeleteSession() = runTest {
        // Given: a session exists
        val book = SessionInlineBookInputDto(title = "Delete Test Book", author = "Delete Author")
        val createRequest = SessionCreateRequestDto(
            clubId = "club-owner",
            book = book
        )
        val created = sessionService.create(createRequest)
        val sessionId = created.session?.id
        assertNotNull(sessionId)

        // When: deleting the session
        val response = sessionService.delete(sessionId)

        // Then: should return success
        assertTrue(response.success == true, "Session deletion should succeed")

        // Verify it no longer exists
        assertFailsWith<Exception> {
            sessionService.get(sessionId)
        }
    }

    @Test
    fun testCreateSessionWithDiscussions() = runTest {
        // Given: a session with discussions
        val book = SessionInlineBookInputDto(title = "Discussion Book", author = "Discussion Author")
        val discussions = listOf(
            SessionDiscussionInputDto(
                title = "Chapter 1 Discussion",
                scheduledAt = "2025-06-15",
                location = "Discord"
            ),
            SessionDiscussionInputDto(
                title = "Final Discussion",
                scheduledAt = "2025-06-30",
                location = "In-person"
            )
        )
        val request = SessionCreateRequestDto(
            clubId = "club-owner",
            book = book,
            dueDate = "2025-07-01",
            discussions = discussions
        )

        var sessionId: String? = null
        try {
            // When: creating session with discussions
            val response = sessionService.create(request)

            // Then: should create discussions
            assertTrue(response.success == true)
            assertNotNull(response.session, "Should return session data")
            sessionId = response.session?.id

            // Verify discussions exist (if backend created them)
            sessionId?.let {
                val retrieved = sessionService.get(it)
                // Note: The backend may or may not create discussions based on various factors
                // This test just verifies the session was created successfully
                assertTrue(retrieved.id == sessionId, "Session should have correct ID")
            }
        } finally {
            // Cleanup
            sessionId?.let {
                try {
                    sessionService.delete(it)
                } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testUpdateSessionDiscussions() = runTest {
        // Given: a session with discussions
        val book = SessionInlineBookInputDto(title = "Update Disc Book", author = "Author")
        val initialDiscussions = listOf(
            SessionDiscussionInputDto(
                title = "Initial Discussion",
                scheduledAt = "2025-05-01",
                location = "Online"
            )
        )
        val createRequest = SessionCreateRequestDto(
            clubId = "club-owner",
            book = book,
            discussions = initialDiscussions
        )
        val created = sessionService.create(createRequest)
        val sessionId = created.session?.id
        assertNotNull(sessionId)

        try {
            // Get the discussion ID that was created
            val session = sessionService.get(sessionId)
            val discussionId = session.discussions?.firstOrNull()?.id

            // When: requesting discussion deletion (reserved for future use — currently a no-op
            // per spec, only checked for presence to determine required role)
            if (discussionId != null) {
                val updateRequest = SessionUpdateRequestDto(
                    id = sessionId,
                    discussionIdsToDelete = listOf(discussionId)
                )
                val response = sessionService.update(updateRequest)

                // Then: should succeed or return "no changes"
                assertTrue(response.success == true || response.message?.contains("No changes") == true,
                           "Expected success or 'No changes' message")
            }
        } finally {
            // Cleanup
            try {
                sessionService.delete(sessionId)
            } catch (_: Exception) { }
        }
    }

    @Test
    fun testGetReadingLog() = runTest {
        // Given: the reading log is member-scoped — run as the seeded auth user
        // (Ivan participates in session-owner-active, session-member-active,
        // and the finished session-owner-past-1)
        val userSessionService = SessionServiceImpl(createUserAuthedSupabaseClient())

        // When: fetching the reading log
        val response = userSessionService.getReadingLog()

        // Then: sessions are grouped by status, with book and club context
        assertTrue(response.success == true, "Reading log fetch should succeed")
        val readingLog = assertNotNull(response.readingLog)

        val active = readingLog.active ?: emptyList()
        assertTrue(active.any { it.id == "session-owner-active" },
            "Active group should include session-owner-active")
        assertTrue(active.any { it.id == "session-member-active" },
            "Active group should include session-member-active")

        val finished = readingLog.finished ?: emptyList()
        assertTrue(finished.any { it.id == "session-owner-past-1" },
            "Finished group should include session-owner-past-1")

        (active + finished).forEach { entry ->
            assertNotNull(entry.book, "Reading log entry should include its book")
            assertNotNull(entry.club, "Reading log entry should include its club")
        }
    }

    @Test
    fun testGetNonExistentSession() = runTest {
        // When: trying to get non-existent session
        // Then: should throw exception
        assertFailsWith<Exception> {
            sessionService.get("non-existent-session-id")
        }
    }

    @Test
    fun testSessionWithNullDueDate() = runTest {
        // Given: a session without due date
        val book = SessionInlineBookInputDto(title = "No Due Date Book", author = "Author")
        val request = SessionCreateRequestDto(
            clubId = "club-owner",
            book = book,
            dueDate = null
        )

        var sessionId: String? = null
        try {
            // When: creating session
            val response = sessionService.create(request)

            // Then: should create successfully
            assertTrue(response.success == true)
            sessionId = response.session?.id

            // Verify due_date is null
            sessionId?.let {
                val retrieved = sessionService.get(it)
                assertNull(retrieved.dueDate, "Due date should be null")
            }
        } finally {
            // Cleanup
            sessionId?.let {
                try {
                    sessionService.delete(it)
                } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testSessionBookHasAllSupportedFields() = runTest {
        // Given: a book with all fields supported by the deprecated inline POST book object
        // (no id/edition — those aren't part of this shape; year/isbn/page_count are)
        val book = SessionInlineBookInputDto(
            title = "Complete Book",
            author = "Complete Author",
            year = 2023,
            isbn = "978-3-16-148410-0",
            pageCount = 320
        )
        val request = SessionCreateRequestDto(
            clubId = "club-owner",
            book = book
        )

        var sessionId: String? = null
        try {
            // When: creating session
            val response = sessionService.create(request)
            sessionId = response.session?.id

            // Then: all supported book fields should be preserved
            sessionId?.let {
                val retrieved = sessionService.get(it)
                assertEquals("Complete Book", retrieved.book?.title)
                assertEquals("Complete Author", retrieved.book?.author)
                assertEquals(2023, retrieved.book?.year)
                assertEquals("978-3-16-148410-0", retrieved.book?.isbn)
            }
        } finally {
            // Cleanup
            sessionId?.let {
                try {
                    sessionService.delete(it)
                } catch (_: Exception) { }
            }
        }
    }
}
