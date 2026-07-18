package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.kluvs.api.models.DiscussionNoteCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionNoteDto
import com.ivangarzab.kluvs.api.models.DiscussionNoteUpdateRequestDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for [DiscussionNoteService] using local Supabase instance with seed data.
 *
 * The discussion-note endpoint is member-scoped (user JWT only), so all calls run
 * as the seeded auth user Ivan Garza (member 1).
 *
 * Ivan's seeded notes (/kluvs-backend/supabase/seed.sql):
 * - note-owner-past-1-ivan on disc-owner-past-1 (The Allegory of the Cave)
 * - note-owner-past-2-ivan on disc-owner-past-2 (The Ideal State)
 * disc-owner-upcoming and disc-owner-future have no notes.
 */
class DiscussionNoteServiceIntegrationTest {

    private suspend fun noteService(): DiscussionNoteService =
        DiscussionNoteServiceImpl(createUserAuthedSupabaseClient())

    @Test
    fun testGetSeededNote() = runTest {
        // Given: Ivan has a seeded note on disc-owner-past-1
        val note = noteService().get("disc-owner-past-1")

        // Then: the note comes back with full data
        assertNotNull(note, "Seeded note should exist")
        assertEquals("disc-owner-past-1", note.discussionId)
        assertEquals(TEST_USER_MEMBER_ID, note.memberId)
        assertEquals(DiscussionNoteDto.Visibility.`private`, note.visibility)
        assertTrue(note.content.contains("Allegory of the Cave"),
            "Note content should match the seeded text")
    }

    @Test
    fun testGetNoteWhenNoneExists() = runTest {
        // Given: Ivan has no note on disc-owner-future
        val note = noteService().get("disc-owner-future")

        // Then: the backend returns a JSON null, mapped to Kotlin null
        assertNull(note, "No note should exist for disc-owner-future")
    }

    @Test
    fun testCreateUpdateDeleteNote() = runTest {
        val service = noteService()
        var noteId: String? = null
        try {
            // When: creating a note on the upcoming discussion
            val created = service.create(
                DiscussionNoteCreateRequestDto(
                    discussionId = "disc-owner-upcoming",
                    content = "Integration test food for thought",
                )
            )
            noteId = created.id

            // Then: the note is created for the authenticated member
            assertEquals("disc-owner-upcoming", created.discussionId)
            assertEquals(TEST_USER_MEMBER_ID, created.memberId)
            assertEquals("Integration test food for thought", created.content)

            // When: updating the content
            val updated = service.update(
                DiscussionNoteUpdateRequestDto(
                    id = noteId,
                    content = "Revised thoughts after more reading",
                )
            )

            // Then: the update persists on re-fetch
            assertEquals("Revised thoughts after more reading", updated.content)
            assertEquals("Revised thoughts after more reading",
                service.get("disc-owner-upcoming")?.content)

            // When: deleting the note (backend responds 204)
            service.delete(noteId)
            noteId = null

            // Then: the discussion has no note again
            assertNull(service.get("disc-owner-upcoming"), "Deleted note should be gone")
        } finally {
            noteId?.let {
                try { service.delete(it) } catch (_: Exception) { }
            }
        }
    }

    @Test
    fun testDuplicateNoteRejected() = runTest {
        // Given: Ivan already has a seeded note on disc-owner-past-1
        // When/Then: creating another note for the same discussion fails (409)
        assertFailsWith<Exception> {
            noteService().create(
                DiscussionNoteCreateRequestDto(
                    discussionId = "disc-owner-past-1",
                    content = "This should be rejected",
                )
            )
        }
    }
}
