package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.DiscussionNoteDto
import com.ivangarzab.kluvs.model.DiscussionNote
import com.ivangarzab.kluvs.model.NoteVisibility

/**
 * Maps a [com.ivangarzab.kluvs.api.models.DiscussionNoteDto] from the API to a
 * [DiscussionNote] domain model.
 */
fun DiscussionNoteDto.toDomain(): DiscussionNote {
    return DiscussionNote(
        id = id,
        discussionId = discussionId,
        memberId = memberId.toString(),
        content = content,
        visibility = when (visibility) {
            DiscussionNoteDto.Visibility.`public` -> NoteVisibility.PUBLIC
            DiscussionNoteDto.Visibility.`private` -> NoteVisibility.PRIVATE
        },
        createdAt = createdAt.parseDateStringOrNull(),
        updatedAt = updatedAt.parseDateStringOrNull()
    )
}
