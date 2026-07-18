package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DiscussionNoteCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionNoteUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.api.DiscussionNoteService
import com.ivangarzab.kluvs.data.remote.mappers.toDomain
import com.ivangarzab.kluvs.model.DiscussionNote

/**
 * Remote data source for the authenticated member's discussion notes.
 *
 * Responsibilities:
 * - Calls [com.ivangarzab.kluvs.data.remote.api.DiscussionNoteService] to fetch/mutate note data from Supabase
 * - Maps DTOs to domain models using mappers
 * - Wraps results in [Result] for error handling
 */
interface DiscussionNoteRemoteDataSource {

    /**
     * Fetches the member's note for the given discussion.
     *
     * Returns null inside the [Result] when no note exists yet.
     */
    suspend fun getNote(discussionId: String): Result<DiscussionNote?>

    /**
     * Creates a note on a discussion.
     */
    suspend fun createNote(request: DiscussionNoteCreateRequestDto): Result<DiscussionNote>

    /**
     * Updates an existing note.
     */
    suspend fun updateNote(request: DiscussionNoteUpdateRequestDto): Result<DiscussionNote>

    /**
     * Deletes a note by ID.
     */
    suspend fun deleteNote(noteId: String): Result<Unit>
}

class DiscussionNoteRemoteDataSourceImpl(
    private val discussionNoteService: DiscussionNoteService
) : DiscussionNoteRemoteDataSource {

    override suspend fun getNote(discussionId: String): Result<DiscussionNote?> {
        return try {
            val note = discussionNoteService.get(discussionId)?.toDomain()
            Bark.d("Fetched note for discussion (ID: $discussionId, Exists: ${note != null})")
            Result.success(note)
        } catch (e: Exception) {
            Bark.e("Failed to fetch note for discussion (ID: $discussionId). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun createNote(request: DiscussionNoteCreateRequestDto): Result<DiscussionNote> {
        return try {
            val note = discussionNoteService.create(request).toDomain()
            Bark.i("Note created (ID: ${note.id})")
            Result.success(note)
        } catch (e: Exception) {
            Bark.e("Failed to create note for discussion (ID: ${request.discussionId}). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun updateNote(request: DiscussionNoteUpdateRequestDto): Result<DiscussionNote> {
        return try {
            val note = discussionNoteService.update(request).toDomain()
            Bark.i("Note updated (ID: ${note.id})")
            Result.success(note)
        } catch (e: Exception) {
            Bark.e("Failed to update note (ID: ${request.id}). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            discussionNoteService.delete(noteId)
            Bark.i("Note deleted (ID: $noteId)")
            Result.success(Unit)
        } catch (e: Exception) {
            Bark.e("Failed to delete note (ID: $noteId). Please retry.", e)
            Result.failure(e)
        }
    }
}
