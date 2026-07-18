package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ProgressCreateRequestDto
import com.ivangarzab.kluvs.api.models.ProgressUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.api.ProgressService
import com.ivangarzab.kluvs.data.remote.mappers.toDomain
import com.ivangarzab.kluvs.model.ReadingProgress

/**
 * Remote data source for the authenticated member's reading progress.
 *
 * Responsibilities:
 * - Calls [com.ivangarzab.kluvs.data.remote.api.ProgressService] to fetch/mutate progress data from Supabase
 * - Maps DTOs to domain models using mappers
 * - Wraps results in [Result] for error handling
 */
interface ProgressRemoteDataSource {

    /**
     * Fetches the member's progress entries, optionally filtered by book,
     * session, or status.
     */
    suspend fun getProgress(
        bookId: Int? = null,
        sessionId: String? = null,
        status: String? = null,
    ): Result<List<ReadingProgress>>

    /**
     * Creates a new progress entry.
     */
    suspend fun createProgress(request: ProgressCreateRequestDto): Result<ReadingProgress>

    /**
     * Updates an existing progress entry.
     */
    suspend fun updateProgress(request: ProgressUpdateRequestDto): Result<ReadingProgress>

    /**
     * Deletes a progress entry by ID.
     */
    suspend fun deleteProgress(progressId: String): Result<Unit>
}

class ProgressRemoteDataSourceImpl(
    private val progressService: ProgressService
) : ProgressRemoteDataSource {

    override suspend fun getProgress(
        bookId: Int?,
        sessionId: String?,
        status: String?,
    ): Result<List<ReadingProgress>> {
        return try {
            val entries = progressService.getAll(bookId, sessionId, status).map { it.toDomain() }
            Bark.d("Fetched progress entries (${entries.size} entries)")
            Result.success(entries)
        } catch (e: Exception) {
            Bark.e("Failed to fetch progress entries. Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun createProgress(request: ProgressCreateRequestDto): Result<ReadingProgress> {
        return try {
            val progress = progressService.create(request).toDomain()
            Bark.i("Progress entry created (ID: ${progress.id})")
            Result.success(progress)
        } catch (e: Exception) {
            Bark.e("Failed to create progress entry for book (ID: ${request.bookId}). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProgress(request: ProgressUpdateRequestDto): Result<ReadingProgress> {
        return try {
            val progress = progressService.update(request).toDomain()
            Bark.i("Progress entry updated (ID: ${progress.id})")
            Result.success(progress)
        } catch (e: Exception) {
            Bark.e("Failed to update progress entry (ID: ${request.id}). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteProgress(progressId: String): Result<Unit> {
        return try {
            val response = progressService.delete(progressId)
            if (response.success == false) {
                throw Exception("Progress deletion failed (ID: $progressId)")
            }
            Bark.i("Progress entry deleted (ID: $progressId)")
            Result.success(Unit)
        } catch (e: Exception) {
            Bark.e("Failed to delete progress entry (ID: $progressId). Please retry.", e)
            Result.failure(e)
        }
    }
}
