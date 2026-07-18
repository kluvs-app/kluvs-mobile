package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ProgressCreateRequestDto
import com.ivangarzab.kluvs.api.models.ProgressUpdateRequestDto
import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.cache.CacheTTL
import com.ivangarzab.kluvs.data.local.source.ProgressLocalDataSource
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

/**
 * Implementation of [ProgressRepository] with TTL-based caching.
 *
 * Implements a cache-aside pattern:
 * - Reads check local cache first (1h TTL), using the freshness of the first
 *   cached entry as a proxy for the whole filtered result set (entries are
 *   always fetched and cached together)
 * - Cache misses fetch from remote and populate cache
 * - Mutations write-through to cache on success
 */
internal class ProgressRepositoryImpl(
    private val progressRemoteDataSource: ProgressRemoteDataSource,
    private val progressLocalDataSource: ProgressLocalDataSource,
    private val cachePolicy: CachePolicy
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

        val cachedEntries = progressLocalDataSource.getProgress(bookId, sessionId, status?.name)
        if (cachedEntries.isNotEmpty()) {
            val lastFetchedAt = progressLocalDataSource.getLastFetchedAt(cachedEntries.first().id)
            if (!cachePolicy.isStale(lastFetchedAt, CacheTTL.PROGRESS)) {
                Bark.d("Cache hit for progress entries")
                return Result.success(cachedEntries)
            }
        }
        Bark.d("Cache miss for progress entries")

        val result = progressRemoteDataSource.getProgress(
            bookId = bookIdInt,
            sessionId = sessionId,
            status = status?.toQueryValue()
        )

        result.onSuccess { entries ->
            Bark.v("Persisting ${entries.size} progress entries to cache")
            try {
                entries.forEach { progressLocalDataSource.insertProgress(it) }
                Bark.d("Progress entries cached (${entries.size})")
            } catch (e: Exception) {
                Bark.e("Progress cache failed. Will use remote data on next fetch.", e)
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch progress entries. Cached data may be unavailable.", error)
        }

        return result
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
        val result = progressRemoteDataSource.createProgress(
            ProgressCreateRequestDto(
                bookId = bookIdInt,
                progressType = type.toCreateDto(),
                sessionId = sessionId,
                currentPage = currentPage,
                percentComplete = percentComplete
            )
        )

        result.onSuccess { progress ->
            Bark.v("Persisting new progress entry to cache (ID: ${progress.id})")
            try {
                progressLocalDataSource.insertProgress(progress)
                Bark.i("Progress entry created and cached (ID: ${progress.id})")
            } catch (e: Exception) {
                Bark.e("Progress cache failed. Will fetch from remote if needed.", e)
            }
        }.onFailure { error ->
            Bark.e("Progress creation failed. Check input and retry.", error)
        }

        return result
    }

    override suspend fun updateProgress(
        progressId: String,
        type: ProgressType,
        currentPage: Int?,
        percentComplete: Float?,
        status: ProgressStatus?,
    ): Result<ReadingProgress> {
        Bark.d("Updating progress entry (ID: $progressId)")
        val result = progressRemoteDataSource.updateProgress(
            ProgressUpdateRequestDto(
                id = progressId,
                progressType = type.toUpdateDto(),
                currentPage = currentPage,
                percentComplete = percentComplete,
                status = status?.toUpdateDto()
            )
        )

        result.onSuccess { progress ->
            Bark.v("Persisting updated progress entry to cache (ID: ${progress.id})")
            try {
                progressLocalDataSource.insertProgress(progress)
                Bark.i("Progress entry updated and cached (ID: ${progress.id})")
            } catch (e: Exception) {
                Bark.e("Progress cache failed. Will fetch updated data from remote.", e)
            }
        }.onFailure { error ->
            Bark.e("Progress update failed. Verify input and retry.", error)
        }

        return result
    }

    override suspend fun deleteProgress(progressId: String): Result<Unit> {
        Bark.d("Deleting progress entry (ID: $progressId)")
        val result = progressRemoteDataSource.deleteProgress(progressId)

        result.onSuccess {
            Bark.v("Removing progress entry from cache (ID: $progressId)")
            progressLocalDataSource.deleteProgress(progressId)
        }.onFailure { error ->
            Bark.e("Progress deletion failed. Verify progress entry exists and retry.", error)
        }

        return result
    }
}
