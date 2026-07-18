package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DiscussionNoteCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionNoteUpdateRequestDto
import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.cache.CacheTTL
import com.ivangarzab.kluvs.data.local.source.DiscussionNoteLocalDataSource
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

/**
 * Implementation of [DiscussionNoteRepository] with TTL-based caching.
 *
 * Implements a cache-aside pattern:
 * - Reads check local cache first (1h TTL)
 * - Cache misses fetch from remote and populate cache
 * - Mutations write-through to cache on success
 */
internal class DiscussionNoteRepositoryImpl(
    private val discussionNoteRemoteDataSource: DiscussionNoteRemoteDataSource,
    private val discussionNoteLocalDataSource: DiscussionNoteLocalDataSource,
    private val cachePolicy: CachePolicy
) : DiscussionNoteRepository {

    override suspend fun getNote(discussionId: String): Result<DiscussionNote?> {
        val cachedNote = discussionNoteLocalDataSource.getNote(discussionId)
        val lastFetchedAt = discussionNoteLocalDataSource.getLastFetchedAt(discussionId)

        if (cachedNote != null && !cachePolicy.isStale(lastFetchedAt, CacheTTL.DISCUSSION_NOTE)) {
            Bark.d("Cache hit for discussion note (discussion ID: $discussionId)")
            return Result.success(cachedNote)
        }
        Bark.d("Cache miss for discussion note (discussion ID: $discussionId)")

        val result = discussionNoteRemoteDataSource.getNote(discussionId)

        result.onSuccess { note ->
            Bark.v("Persisting discussion note to cache (discussion ID: $discussionId)")
            try {
                if (note != null) {
                    discussionNoteLocalDataSource.insertNote(note)
                    Bark.d("Discussion note cached (ID: ${note.id})")
                } else if (cachedNote != null) {
                    discussionNoteLocalDataSource.deleteNote(cachedNote.id)
                }
            } catch (e: Exception) {
                Bark.e("Discussion note cache failed. Will use remote data on next fetch.", e)
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch discussion note. Cached data may be unavailable.", error)
        }

        return result
    }

    override suspend fun createNote(discussionId: String, content: String): Result<DiscussionNote> {
        Bark.d("Creating note for discussion (ID: $discussionId)")
        val result = discussionNoteRemoteDataSource.createNote(
            DiscussionNoteCreateRequestDto(
                discussionId = discussionId,
                content = content
            )
        )

        result.onSuccess { note ->
            Bark.v("Persisting new discussion note to cache (ID: ${note.id})")
            try {
                discussionNoteLocalDataSource.insertNote(note)
                Bark.i("Discussion note created and cached (ID: ${note.id})")
            } catch (e: Exception) {
                Bark.e("Discussion note cache failed. Will fetch from remote if needed.", e)
            }
        }.onFailure { error ->
            Bark.e("Discussion note creation failed. Check input and retry.", error)
        }

        return result
    }

    override suspend fun updateNote(noteId: String, content: String): Result<DiscussionNote> {
        Bark.d("Updating note (ID: $noteId)")
        val result = discussionNoteRemoteDataSource.updateNote(
            DiscussionNoteUpdateRequestDto(
                id = noteId,
                content = content
            )
        )

        result.onSuccess { note ->
            Bark.v("Persisting updated discussion note to cache (ID: ${note.id})")
            try {
                discussionNoteLocalDataSource.insertNote(note)
                Bark.i("Discussion note updated and cached (ID: ${note.id})")
            } catch (e: Exception) {
                Bark.e("Discussion note cache failed. Will fetch updated data from remote.", e)
            }
        }.onFailure { error ->
            Bark.e("Discussion note update failed. Verify input and retry.", error)
        }

        return result
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> {
        Bark.d("Deleting note (ID: $noteId)")
        val result = discussionNoteRemoteDataSource.deleteNote(noteId)

        result.onSuccess {
            Bark.v("Removing discussion note from cache (ID: $noteId)")
            discussionNoteLocalDataSource.deleteNote(noteId)
        }.onFailure { error ->
            Bark.e("Discussion note deletion failed. Verify note exists and retry.", error)
        }

        return result
    }
}
