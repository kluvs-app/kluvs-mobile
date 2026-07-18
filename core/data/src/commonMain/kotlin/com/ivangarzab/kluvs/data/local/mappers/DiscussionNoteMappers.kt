package com.ivangarzab.kluvs.data.local.mappers

import com.ivangarzab.kluvs.data.remote.mappers.parseDateStringOrNull
import com.ivangarzab.kluvs.database.entities.DiscussionNoteEntity
import com.ivangarzab.kluvs.model.DiscussionNote
import com.ivangarzab.kluvs.model.NoteVisibility
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Maps a [DiscussionNoteEntity] from the local database to a [DiscussionNote] domain model.
 */
fun DiscussionNoteEntity.toDomain(): DiscussionNote {
    return DiscussionNote(
        id = id,
        discussionId = discussionId,
        memberId = memberId,
        content = content,
        visibility = NoteVisibility.valueOf(visibility),
        createdAt = createdAt.parseDateStringOrNull(),
        updatedAt = updatedAt.parseDateStringOrNull()
    )
}

/**
 * Maps a [DiscussionNote] domain model to a [DiscussionNoteEntity] for local database storage.
 * Sets lastFetchedAt to current time.
 */
@OptIn(ExperimentalTime::class)
fun DiscussionNote.toEntity(): DiscussionNoteEntity {
    return DiscussionNoteEntity(
        discussionId = discussionId,
        id = id,
        memberId = memberId,
        content = content,
        visibility = visibility.name,
        createdAt = createdAt?.toString(),
        updatedAt = updatedAt?.toString(),
        lastFetchedAt = Clock.System.now().toEpochMilliseconds()
    )
}
