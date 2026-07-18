package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DiscussionCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionUpdateRequestDto
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
    private val discussionRemoteDataSource: DiscussionRemoteDataSource
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
        )
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
        )
    }

    override suspend fun deleteDiscussion(discussionId: String): Result<Unit> {
        Bark.d("Deleting discussion (ID: $discussionId)")
        return discussionRemoteDataSource.deleteDiscussion(discussionId)
    }
}
