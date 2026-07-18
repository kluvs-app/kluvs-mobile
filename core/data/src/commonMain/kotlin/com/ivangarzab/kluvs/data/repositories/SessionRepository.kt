package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.kluvs.api.models.SessionCreateRequestDto
import com.ivangarzab.kluvs.api.models.SessionBookPatchInputDto
import com.ivangarzab.kluvs.api.models.SessionUpdateRequestDto
import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.cache.CacheTTL
import com.ivangarzab.kluvs.data.local.source.SessionLocalDataSource
import com.ivangarzab.kluvs.data.remote.mappers.toDto
import com.ivangarzab.kluvs.data.remote.source.SessionRemoteDataSource
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.ReadingLog
import com.ivangarzab.kluvs.model.Session
import com.ivangarzab.bark.Bark
import kotlinx.datetime.LocalDateTime

/**
 * Repository for managing Session data.
 *
 * This repository abstracts the data source layer and provides a clean API for accessing
 * session-related data. Currently delegates to remote data source, but can be extended
 * to support local caching and offline capabilities.
 */
interface SessionRepository {

    /**
     * Retrieves a single session by its ID.
     *
     * @param sessionId The ID of the session to retrieve
     * @param forceRefresh If true, bypasses cache and fetches from remote
     * @return Result containing the Session (with nested book, discussions, shame list, etc.)
     *         if successful, or an error if the operation failed
     */
    suspend fun getSession(sessionId: String, forceRefresh: Boolean = false): Result<Session>

    /**
     * Creates a new reading session.
     *
     * @param clubId The ID of the club this session belongs to
     * @param book The book for this reading session (must already be registered — its [Book.id] is used)
     * @param dueDate Optional due date for completing the book
     * @param discussions Optional list of discussions to create with this session
     * @return Result containing the created Session if successful, or an error if the operation failed
     */
    suspend fun createSession(
        clubId: String,
        book: Book,
        dueDate: LocalDateTime?,
        discussions: List<Discussion>? = null
    ): Result<Session>

    /**
     * Updates an existing session.
     *
     * Uses PATCH semantics - only fields that are non-null will be updated.
     * Pass null for any field you want to leave unchanged.
     *
     * Note: the backend's PUT /session response never includes the updated session, so
     * on success this re-fetches the session via [getSession] to return fresh data.
     *
     * @param sessionId The ID of the session to update
     * @param book Optional book whose [Book.title]/[Book.author] should be patched on the
     *             session's existing book (the backend only supports patching those two
     *             fields here — it cannot re-point the session at a different book)
     * @param dueDate Optional new due date (null = don't update due date)
     * @param discussions Reserved for future use by the backend — currently only checked
     *                     for presence to determine the required role; no discussion
     *                     records are created/updated by this endpoint
     * @param discussionIdsToDelete Reserved for future use by the backend — currently only
     *                               checked for presence; no discussion records are deleted
     *                               by this endpoint
     * @return Result containing the updated Session if successful, or an error if the operation failed
     */
    suspend fun updateSession(
        sessionId: String,
        book: Book? = null,
        dueDate: LocalDateTime? = null,
        discussions: List<Discussion>? = null,
        discussionIdsToDelete: List<String>? = null
    ): Result<Session>

    /**
     * Deletes a session by its ID.
     *
     * @param sessionId The ID of the session to delete
     * @return Result containing success message if deletion was successful, or an error if the operation failed
     */
    suspend fun deleteSession(sessionId: String): Result<String>

    /**
     * Retrieves the authenticated member's reading log — all their sessions
     * grouped into active and finished. Requires a signed-in user session.
     *
     * @return Result containing the [ReadingLog] if successful, or an error if the operation failed
     */
    suspend fun getReadingLog(): Result<ReadingLog>
}

/**
 * Implementation of [SessionRepository] with TTL-based caching.
 *
 * Implements a cache-aside pattern:
 * - Read operations check local cache first (6h TTL)
 * - Cache misses fetch from remote and populate cache
 * - Write operations invalidate cache and delegate to remote
 *
 * Note: The API returns nested data (book, discussions, shame list) with Session responses.
 * Future implementations may decompose this nested data and coordinate with other
 * repositories for caching purposes.
 */
internal class SessionRepositoryImpl(
    private val sessionRemoteDataSource: SessionRemoteDataSource,
    private val sessionLocalDataSource: SessionLocalDataSource,
    private val cachePolicy: CachePolicy
) : SessionRepository {

    override suspend fun getSession(sessionId: String, forceRefresh: Boolean): Result<Session> {
        if (!forceRefresh) {
            val cachedSession = sessionLocalDataSource.getSession(sessionId)
            val lastFetchedAt = sessionLocalDataSource.getLastFetchedAt(sessionId)

            if (cachedSession != null && !cachePolicy.isStale(lastFetchedAt, CacheTTL.SESSION)) {
                Bark.d("Cache hit for session (ID: $sessionId)")
                return Result.success(cachedSession)
            }
            Bark.d("Cache miss for session (ID: $sessionId)")
        }

        Bark.d("Fetching session from remote (ID: $sessionId)")
        val result = sessionRemoteDataSource.getSession(sessionId)

        result.onSuccess { session ->
            Bark.v("Persisting session to cache (ID: ${session.id})")
            try {
                sessionLocalDataSource.insertSession(session)
                Bark.d("Session cached (ID: ${session.id})")
            } catch (e: Exception) {
                Bark.e("Session cache failed. Will use remote data on next fetch.", e)
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch session. Cached data may be unavailable.", error)
        }

        return result
    }

    override suspend fun createSession(
        clubId: String,
        book: Book,
        dueDate: LocalDateTime?,
        discussions: List<Discussion>?
    ): Result<Session> {
        Bark.d("Creating session for club (ID: $clubId): ${book.title}")
        val result = sessionRemoteDataSource.createSession(
            SessionCreateRequestDto(
                clubId = clubId,
                bookId = book.id.toIntOrNull(),
                dueDate = dueDate?.toString(),
                discussions = discussions?.map { it.toDto() }
            )
        )

        result.onSuccess { session ->
            Bark.v("Persisting new session to cache (ID: ${session.id})")
            try {
                sessionLocalDataSource.insertSession(session)
                Bark.i("Session created and cached (ID: ${session.id})")
            } catch (e: Exception) {
                Bark.e("Session cache failed. Will fetch from remote if needed.", e)
            }
        }.onFailure { error ->
            Bark.e("Session creation failed. Check input and retry.", error)
        }

        return result
    }

    override suspend fun updateSession(
        sessionId: String,
        book: Book?,
        dueDate: LocalDateTime?,
        discussions: List<Discussion>?,
        discussionIdsToDelete: List<String>?
    ): Result<Session> {
        Bark.d("Updating session (ID: $sessionId)")
        val updateResult = sessionRemoteDataSource.updateSession(
            SessionUpdateRequestDto(
                id = sessionId,
                dueDate = dueDate?.toString(),
                book = book?.let { SessionBookPatchInputDto(title = it.title, author = it.author) },
                discussions = discussions?.map { it.id },
                discussionIdsToDelete = discussionIdsToDelete
            )
        )

        if (updateResult.isFailure) {
            Bark.e("Session update failed. Verify input and retry.", updateResult.exceptionOrNull())
            return Result.failure(updateResult.exceptionOrNull() ?: Exception("Session update failed"))
        }

        // PUT /session never returns the updated session, so re-fetch it for fresh data.
        val result = sessionRemoteDataSource.getSession(sessionId)

        result.onSuccess { session ->
            Bark.v("Persisting updated session to cache (ID: ${session.id})")
            try {
                sessionLocalDataSource.insertSession(session)
                Bark.i("Session updated and cached (ID: ${session.id})")
            } catch (e: Exception) {
                Bark.e("Session cache failed. Will fetch updated data from remote.", e)
            }
        }.onFailure { error ->
            Bark.e("Session updated, but re-fetching fresh data failed.", error)
        }

        return result
    }

    override suspend fun deleteSession(sessionId: String): Result<String> {
        Bark.d("Deleting session (ID: $sessionId)")
        val result = sessionRemoteDataSource.deleteSession(sessionId)

        result.onSuccess {
            Bark.v("Removing session from cache (ID: $sessionId)")
            sessionLocalDataSource.deleteSession(sessionId)
            Bark.i("Session deleted (ID: $sessionId)")
        }.onFailure { error ->
            Bark.e("Session deletion failed. Verify session exists and retry.", error)
        }

        return result
    }

    override suspend fun getReadingLog(): Result<ReadingLog> {
        Bark.d("Fetching reading log from remote")
        return sessionRemoteDataSource.getReadingLog()
    }
}
