package com.ivangarzab.kluvs.data.local.mappers

import com.ivangarzab.kluvs.database.entities.DiscussionNoteEntity
import com.ivangarzab.kluvs.model.DiscussionNote
import com.ivangarzab.kluvs.model.NoteVisibility
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DiscussionNoteMappersTest {

    @Test
    fun testDiscussionNoteEntity_toDomain() {
        val entity = DiscussionNoteEntity(
            discussionId = "discussion-1",
            id = "note-1",
            memberId = "member-1",
            content = "Great chapter!",
            visibility = "PUBLIC",
            createdAt = "2024-02-01T12:00:00",
            updatedAt = null,
            lastFetchedAt = 1234567890L
        )

        val domain = entity.toDomain()

        assertEquals("note-1", domain.id)
        assertEquals("discussion-1", domain.discussionId)
        assertEquals("member-1", domain.memberId)
        assertEquals("Great chapter!", domain.content)
        assertEquals(NoteVisibility.PUBLIC, domain.visibility)
        assertEquals(LocalDateTime.parse("2024-02-01T12:00:00"), domain.createdAt)
    }

    @Test
    fun testDiscussionNote_toEntity() {
        val domain = DiscussionNote(
            id = "note-1",
            discussionId = "discussion-1",
            memberId = "member-1",
            content = "My thoughts",
            visibility = NoteVisibility.PRIVATE,
            updatedAt = LocalDateTime.parse("2024-03-01T12:00:00")
        )

        val entity = domain.toEntity()

        assertEquals("discussion-1", entity.discussionId)
        assertEquals("note-1", entity.id)
        assertEquals("member-1", entity.memberId)
        assertEquals("PRIVATE", entity.visibility)
        assertEquals("2024-03-01T12:00", entity.updatedAt)
        assertNotNull(entity.lastFetchedAt)
    }
}
