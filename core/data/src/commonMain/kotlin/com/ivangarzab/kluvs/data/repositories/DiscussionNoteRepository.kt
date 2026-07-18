package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DiscussionNoteCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionNoteUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.source.DiscussionNoteRemoteDataSource
import com.ivangarzab.kluvs.model.DiscussionNote

/**
 * Repository for the authenticated member's discussion notes.
 *
 * Each member has at most one note per discussion. All operations are
 * member-scoped, so a signed-in user session is required.
 */
interface DiscussionNoteRepository {

    /**
     * Retrieves the member's note for a discussion.
     *
     * @param discussionId The ID of the discussion
     * @return Result containing the [DiscussionNote], or null if no note exists yet
     */
    suspend fun getNote(discussionId: String): Result<DiscussionNote?>

    /**
     * Creates the member's note on a discussion.
     *
     * @param discussionId The ID of the discussion to note
     * @param content The note's content
     * @return Result containing the created [DiscussionNote] if successful
     */
    suspend fun createNote(discussionId: String, content: String): Result<DiscussionNote>

    /**
     * Updates an existing note's content.
     *
     * @param noteId The ID of the note to update
     * @param content The new content
     * @return Result containing the updated [DiscussionNote] if successful
     */
    suspend fun updateNote(noteId: String, content: String): Result<DiscussionNote>

    /**
     * Deletes a note.
     *
     * @param noteId The ID of the note to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteNote(noteId: String): Result<Unit>
}

internal class DiscussionNoteRepositoryImpl(
    private val discussionNoteRemoteDataSource: DiscussionNoteRemoteDataSource
) : DiscussionNoteRepository {

    override suspend fun getNote(discussionId: String): Result<DiscussionNote?> {
        return discussionNoteRemoteDataSource.getNote(discussionId)
    }

    override suspend fun createNote(discussionId: String, content: String): Result<DiscussionNote> {
        Bark.d("Creating note for discussion (ID: $discussionId)")
        return discussionNoteRemoteDataSource.createNote(
            DiscussionNoteCreateRequestDto(
                discussionId = discussionId,
                content = content
            )
        )
    }

    override suspend fun updateNote(noteId: String, content: String): Result<DiscussionNote> {
        Bark.d("Updating note (ID: $noteId)")
        return discussionNoteRemoteDataSource.updateNote(
            DiscussionNoteUpdateRequestDto(
                id = noteId,
                content = content
            )
        )
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> {
        Bark.d("Deleting note (ID: $noteId)")
        return discussionNoteRemoteDataSource.deleteNote(noteId)
    }
}
