package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DiscussionCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.api.DiscussionService
import com.ivangarzab.kluvs.data.remote.mappers.toDomain
import com.ivangarzab.kluvs.model.Discussion

/**
 * Remote data source for Discussion operations.
 *
 * Responsibilities:
 * - Calls [com.ivangarzab.kluvs.data.remote.api.DiscussionService] to mutate discussion data from Supabase
 * - Maps DTOs to domain models using mappers
 * - Wraps results in [Result] for error handling
 *
 * Note: discussions are read through their parent session (see
 * [SessionRemoteDataSource.getSession]) — this data source only mutates them.
 */
interface DiscussionRemoteDataSource {

    /**
     * Creates a new discussion on a session.
     */
    suspend fun createDiscussion(request: DiscussionCreateRequestDto): Result<Discussion>

    /**
     * Updates an existing discussion.
     */
    suspend fun updateDiscussion(request: DiscussionUpdateRequestDto): Result<Discussion>

    /**
     * Deletes a discussion by ID.
     */
    suspend fun deleteDiscussion(discussionId: String): Result<Unit>
}

class DiscussionRemoteDataSourceImpl(
    private val discussionService: DiscussionService
) : DiscussionRemoteDataSource {

    override suspend fun createDiscussion(request: DiscussionCreateRequestDto): Result<Discussion> {
        return try {
            val discussion = discussionService.create(request).toDomain()
            Bark.i("Discussion created (ID: ${discussion.id})")
            Result.success(discussion)
        } catch (e: Exception) {
            Bark.e("Failed to create discussion for session (ID: ${request.sessionId}). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun updateDiscussion(request: DiscussionUpdateRequestDto): Result<Discussion> {
        return try {
            val discussion = discussionService.update(request).toDomain()
            Bark.i("Discussion updated (ID: ${discussion.id})")
            Result.success(discussion)
        } catch (e: Exception) {
            Bark.e("Failed to update discussion (ID: ${request.id}). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteDiscussion(discussionId: String): Result<Unit> {
        return try {
            discussionService.delete(discussionId)
            Bark.i("Discussion deleted (ID: $discussionId)")
            Result.success(Unit)
        } catch (e: Exception) {
            Bark.e("Failed to delete discussion (ID: $discussionId). Please retry.", e)
            Result.failure(e)
        }
    }
}
