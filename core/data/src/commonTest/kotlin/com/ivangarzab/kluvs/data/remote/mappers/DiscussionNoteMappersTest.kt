package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.DiscussionNoteDto
import com.ivangarzab.kluvs.model.NoteVisibility
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DiscussionNoteMappersTest {

    @Test
    fun `DiscussionNoteDto toDomain maps private note`() {
        // Given: A private note
        val dto = DiscussionNoteDto(
            id = "note-1",
            discussionId = "disc-1",
            memberId = 1,
            content = "Great chapter on dragons",
            visibility = DiscussionNoteDto.Visibility.`private`,
            createdAt = "2026-07-01T10:30:00",
            updatedAt = "2026-07-02T11:00:00"
        )

        // When: Mapping to domain
        val note = dto.toDomain()

        // Then: All fields map over
        assertEquals("note-1", note.id)
        assertEquals("disc-1", note.discussionId)
        assertEquals("1", note.memberId)
        assertEquals("Great chapter on dragons", note.content)
        assertEquals(NoteVisibility.PRIVATE, note.visibility)
        assertNotNull(note.createdAt)
        assertNotNull(note.updatedAt)
    }

    @Test
    fun `DiscussionNoteDto toDomain maps public visibility`() {
        val dto = DiscussionNoteDto(
            id = "note-2",
            discussionId = "disc-1",
            memberId = 1,
            content = "Shared thoughts",
            visibility = DiscussionNoteDto.Visibility.`public`,
            createdAt = "2026-07-01T10:30:00",
            updatedAt = "2026-07-01T10:30:00"
        )

        assertEquals(NoteVisibility.PUBLIC, dto.toDomain().visibility)
    }
}
