package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ProgressCreateRequestDto
import com.ivangarzab.kluvs.api.models.ProgressUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.mappers.toCreateDto
import com.ivangarzab.kluvs.data.remote.mappers.toQueryValue
import com.ivangarzab.kluvs.data.remote.mappers.toUpdateDto
import com.ivangarzab.kluvs.data.remote.source.ProgressRemoteDataSource
import com.ivangarzab.kluvs.model.ProgressStatus
import com.ivangarzab.kluvs.model.ProgressType
import com.ivangarzab.kluvs.model.ReadingProgress

/**
 * Repository for the authenticated member's reading progress.
 *
 * All operations are member-scoped: the backend resolves the member from the
 * caller's auth token, so a signed-in user session is required.
 */
interface ProgressRepository {

    /**
     * Retrieves the member's progress entries, optionally filtered.
     *
     * @param bookId Only entries for this book (null = all books)
     * @param sessionId Only entries tied to this session (null = all sessions)
     * @param status Only entries with this status (null = all statuses)
     * @return Result containing the matching [ReadingProgress] entries if successful
     */
    suspend fun getProgress(
        bookId: String? = null,
        sessionId: String? = null,
        status: ProgressStatus? = null,
    ): Result<List<ReadingProgress>>

    /**
     * Starts tracking progress on a book.
     *
     * @param bookId The ID of the book to track
     * @param type How progress is measured (page or percent)
     * @param currentPage Starting page, when [type] is [ProgressType.PAGE]
     * @param percentComplete Starting percentage, when [type] is [ProgressType.PERCENT]
     * @param sessionId Optional session to tie this progress to
     * @return Result containing the created [ReadingProgress] if successful
     */
    suspend fun createProgress(
        bookId: String,
        type: ProgressType,
        currentPage: Int? = null,
        percentComplete: Float? = null,
        sessionId: String? = null,
    ): Result<ReadingProgress>

    /**
     * Updates an existing progress entry.
     *
     * Uses PATCH semantics for the optional fields — only non-null values are updated.
     *
     * @param progressId The ID of the progress entry to update
     * @param type How progress is measured (required by the backend on every update)
     * @param currentPage New current page (null = don't update)
     * @param percentComplete New percentage (null = don't update)
     * @param status New status, e.g. [ProgressStatus.COMPLETED] to finish the book (null = don't update)
     * @return Result containing the updated [ReadingProgress] if successful
     */
    suspend fun updateProgress(
        progressId: String,
        type: ProgressType,
        currentPage: Int? = null,
        percentComplete: Float? = null,
        status: ProgressStatus? = null,
    ): Result<ReadingProgress>

    /**
     * Deletes a progress entry.
     *
     * @param progressId The ID of the progress entry to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteProgress(progressId: String): Result<Unit>
}

internal class ProgressRepositoryImpl(
    private val progressRemoteDataSource: ProgressRemoteDataSource
) : ProgressRepository {

    override suspend fun getProgress(
        bookId: String?,
        sessionId: String?,
        status: ProgressStatus?,
    ): Result<List<ReadingProgress>> {
        val bookIdInt = bookId?.let {
            it.toIntOrNull()
                ?: return Result.failure(IllegalArgumentException("Invalid book ID: $it"))
        }
        return progressRemoteDataSource.getProgress(
            bookId = bookIdInt,
            sessionId = sessionId,
            status = status?.toQueryValue()
        )
    }

    override suspend fun createProgress(
        bookId: String,
        type: ProgressType,
        currentPage: Int?,
        percentComplete: Float?,
        sessionId: String?,
    ): Result<ReadingProgress> {
        val bookIdInt = bookId.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid book ID: $bookId"))
        Bark.d("Creating progress entry for book (ID: $bookId)")
        return progressRemoteDataSource.createProgress(
            ProgressCreateRequestDto(
                bookId = bookIdInt,
                progressType = type.toCreateDto(),
                sessionId = sessionId,
                currentPage = currentPage,
                percentComplete = percentComplete
            )
        )
    }

    override suspend fun updateProgress(
        progressId: String,
        type: ProgressType,
        currentPage: Int?,
        percentComplete: Float?,
        status: ProgressStatus?,
    ): Result<ReadingProgress> {
        Bark.d("Updating progress entry (ID: $progressId)")
        return progressRemoteDataSource.updateProgress(
            ProgressUpdateRequestDto(
                id = progressId,
                progressType = type.toUpdateDto(),
                currentPage = currentPage,
                percentComplete = percentComplete,
                status = status?.toUpdateDto()
            )
        )
    }

    override suspend fun deleteProgress(progressId: String): Result<Unit> {
        Bark.d("Deleting progress entry (ID: $progressId)")
        return progressRemoteDataSource.deleteProgress(progressId)
    }
}
