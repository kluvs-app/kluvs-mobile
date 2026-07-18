package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.DiscussionCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionUpdateRequestDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for [DiscussionService] using local Supabase instance with seed data.
 *
 * Discussion mutations require owner/admin role in the discussion's club, so all
 * calls run as the seeded auth user Ivan Garza — owner of club-owner, whose active
 * session is session-owner-active.
 *
 * Tests create their own discussions and clean up, leaving the four seeded
 * discussions (disc-owner-*) untouched.
 */
class DiscussionServiceIntegrationTest {

    private val activeSessionId = "session-owner-active"

    private suspend fun discussionService(): DiscussionService =
        DiscussionServiceImpl(createUserAuthedSupabaseClient())

    @Test
    fun testCreateDiscussion() = runTest {
        val service = discussionService()
        var discussionId: String? = null
        try {
            // When: creating a discussion on the active session
            val response = service.create(
                DiscussionCreateRequestDto(
                    sessionId = activeSessionId,
                    title = "Integration Test Discussion",
                    scheduledAt = "2026-08-01T19:00:00Z",
                    location = "Discord Voice Channel",
                )
            )
            discussionId = response.id

            // Then: the discussion is returned with the requested values
            assertEquals(activeSessionId, response.sessionId)
            assertEquals("Integration Test Discussion", response.title)
            assertEquals("Discord Voice Channel", response.location)
        } finally {
            discussionId?.let {
                try { service.delete(it) } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testUpdateDiscussion() = runTest {
        val service = discussionService()
        var discussionId: String? = null
        try {
            // Given: a discussion exists
            val created = service.create(
                DiscussionCreateRequestDto(
                    sessionId = activeSessionId,
                    title = "Original Title",
                    scheduledAt = "2026-08-08T19:00:00Z",
                    location = "Online",
                )
            )
            discussionId = created.id

            // When: updating title and location
            val response = service.update(
                DiscussionUpdateRequestDto(
                    id = discussionId,
                    title = "Updated Title",
                    location = "In-person",
                )
            )

            // Then: the changes are reflected
            assertEquals("Updated Title", response.title)
            assertEquals("In-person", response.location)
        } finally {
            discussionId?.let {
                try { service.delete(it) } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testDeleteDiscussion() = runTest {
        val service = discussionService()

        // Given: a discussion exists
        val created = service.create(
            DiscussionCreateRequestDto(
                sessionId = activeSessionId,
                title = "Discussion To Delete",
                scheduledAt = "2026-08-15T19:00:00Z",
            )
        )

        // When: deleting it (backend responds 204)
        service.delete(created.id)

        // Then: it no longer appears on the parent session
        val session = SessionServiceImpl(createBotSupabaseClient()).get(activeSessionId)
        assertTrue(session.discussions?.none { it.id == created.id } != false,
            "Deleted discussion should not appear on the session")
    }
}
