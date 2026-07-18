package com.ivangarzab.kluvs.database.entities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DiscussionNoteEntityTest {

    @Test
    fun testDiscussionNoteEntity_creation() {
        val entity = DiscussionNoteEntity(
            discussionId = "discussion-1",
            id = "note-1",
            memberId = "member-1",
            content = "Great chapter!",
            visibility = "PRIVATE",
            createdAt = "2024-02-01T12:00:00Z",
            updatedAt = null,
            lastFetchedAt = 1234567890L
        )

        assertEquals("discussion-1", entity.discussionId)
        assertEquals("note-1", entity.id)
        assertEquals("member-1", entity.memberId)
        assertEquals("Great chapter!", entity.content)
        assertEquals("PRIVATE", entity.visibility)
        assertEquals("2024-02-01T12:00:00Z", entity.createdAt)
        assertEquals(null, entity.updatedAt)
    }

    @Test
    fun testDiscussionNoteEntity_copy() {
        val original = DiscussionNoteEntity(
            discussionId = "discussion-1",
            id = "note-1",
            memberId = "member-1",
            content = "Great chapter!",
            visibility = "PRIVATE",
            createdAt = null,
            updatedAt = null,
            lastFetchedAt = 1234567890L
        )

        val updated = original.copy(content = "Updated thoughts", updatedAt = "2024-03-01T12:00:00Z")

        assertEquals("note-1", updated.id)
        assertEquals("Updated thoughts", updated.content)
        assertEquals("2024-03-01T12:00:00Z", updated.updatedAt)
    }

    @Test
    fun testDiscussionNoteEntity_equality() {
        val note1 = DiscussionNoteEntity("discussion-1", "note-1", "member-1", "text", "PRIVATE", null, null, 1234567890L)
        val note2 = DiscussionNoteEntity("discussion-1", "note-1", "member-1", "text", "PRIVATE", null, null, 1234567890L)
        val note3 = DiscussionNoteEntity("discussion-2", "note-2", "member-2", "other", "PUBLIC", null, null, 1234567890L)

        assertEquals(note1, note2)
        assertNotEquals(note1, note3)
    }
}
