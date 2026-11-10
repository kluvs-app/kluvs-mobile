package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DiscussionCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionUpdateRequestDto
import com.ivangarzab.kluvs.data.local.source.DiscussionLocalDataSource
import com.ivangarzab.kluvs.data.remote.source.DiscussionRemoteDataSource
import com.ivangarzab.kluvs.model.Discussion
import kotlinx.datetime.LocalDateTime

/**
 * Repository for managing Discussion data.
 *
 * Discussions are read through their parent session (see [SessionRepository.getSession]);
 * this repository covers standalone create/update/delete operations on them.
 */
interface DiscussionRepository {

    /**
     * Creates a new discussion on a session.
     *
     * @param sessionId The ID of the session the discussion belongs to
     * @param title The discussion's title
     * @param date When the discussion is scheduled for
     * @param location Optional location (physical or virtual)
     * @return Result containing the created [Discussion] if successful
     */
    suspend fun createDiscussion(
        sessionId: String,
        title: String,
        date: LocalDateTime,
        location: String? = null
    ): Result<Discussion>

    /**
     * Updates an existing discussion.
     *
     * Uses PATCH semantics — only non-null fields are updated.
     *
     * @param discussionId The ID of the discussion to update
     * @param title New title (null = don't update)
     * @param date New scheduled date (null = don't update)
     * @param location New location (null = don't update)
     * @return Result containing the updated [Discussion] if successful
     */
    suspend fun updateDiscussion(
        discussionId: String,
        title: String? = null,
        date: LocalDateTime? = null,
        location: String? = null
    ): Result<Discussion>

    /**
     * Deletes a discussion.
     *
     * @param discussionId The ID of the discussion to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteDiscussion(discussionId: String): Result<Unit>
}

internal class DiscussionRepositoryImpl(
    private val discussionRemoteDataSource: DiscussionRemoteDataSource,
    private val discussionLocalDataSource: DiscussionLocalDataSource
) : DiscussionRepository {

    override suspend fun createDiscussion(
        sessionId: String,
        title: String,
        date: LocalDateTime,
        location: String?
    ): Result<Discussion> {
        Bark.d("Creating discussion for session (ID: $sessionId): $title")
        return discussionRemoteDataSource.createDiscussion(
            DiscussionCreateRequestDto(
                sessionId = sessionId,
                title = title,
                scheduledAt = date.toString(),
                location = location
            )
        ).onSuccess {
            invalidate("club of session (ID: $sessionId) after creating a discussion") {
                discussionLocalDataSource.markClubStaleForSession(sessionId)
            }
        }
    }

    override suspend fun updateDiscussion(
        discussionId: String,
        title: String?,
        date: LocalDateTime?,
        location: String?
    ): Result<Discussion> {
        Bark.d("Updating discussion (ID: $discussionId)")
        return discussionRemoteDataSource.updateDiscussion(
            DiscussionUpdateRequestDto(
                id = discussionId,
                title = title,
                scheduledAt = date?.toString(),
                location = location
            )
        ).onSuccess {
            invalidate("club of discussion (ID: $discussionId) after update") {
                discussionLocalDataSource.markClubStaleForDiscussion(discussionId)
            }
        }
    }

    override suspend fun deleteDiscussion(discussionId: String): Result<Unit> {
        Bark.d("Deleting discussion (ID: $discussionId)")
        return discussionRemoteDataSource.deleteDiscussion(discussionId).onSuccess {
            invalidate("discussion (ID: $discussionId) after deletion") {
                discussionLocalDataSource.deleteDiscussion(discussionId)
            }
        }
    }

    /**
     * Runs a post-mutation cache cleanup. The mutation already succeeded remotely, so a cache
     * failure is logged rather than surfaced; the next forced refresh corrects the cache.
     */
    private suspend fun invalidate(what: String, block: suspend () -> Unit) {
        try {
            block()
            Bark.v("Invalidated cache for $what")
        } catch (e: Exception) {
            Bark.e("Failed to invalidate cache for $what. Pull to refresh will correct it.", e)
        }
    }
}
