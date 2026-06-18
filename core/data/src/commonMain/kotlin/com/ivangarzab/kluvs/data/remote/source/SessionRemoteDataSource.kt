package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.SessionCreateRequestDto
import com.ivangarzab.kluvs.api.models.SessionUpdateRequestDto
import com.ivangarzab.kluvs.data.remote.api.SessionService
import com.ivangarzab.kluvs.data.remote.mappers.toDomain
import com.ivangarzab.kluvs.model.Session

/**
 * Remote data source for Session operations.
 *
 * Responsibilities:
 * - Calls [com.ivangarzab.kluvs.data.remote.api.SessionService] to fetch/mutate session data from Supabase
 * - Maps DTOs to domain models using mappers
 * - Wraps results in [Result] for error handling
 */
interface SessionRemoteDataSource {

    /**
     * Fetches a session by ID.
     *
     * Returns a [Session] with all nested relations populated:
     * - book (full Book object)
     * - discussions (full Discussion objects)
     * - clubId (extracted from nested Club)
     */
    suspend fun getSession(sessionId: String): Result<Session>

    /**
     * Creates a new session.
     *
     * Returns the created [Session] with nested relations if available.
     */
    suspend fun createSession(request: SessionCreateRequestDto): Result<Session>

    /**
     * Updates an existing session.
     *
     * Note: PUT /session's response never includes the updated session data
     * (confirmed via the generated response — every branch is just
     * success/message plus operation-specific metadata). Callers that need the
     * fresh [Session] must follow up with [getSession].
     */
    suspend fun updateSession(request: SessionUpdateRequestDto): Result<Unit>

    /**
     * Deletes a session by ID.
     *
     * Returns success message on successful deletion.
     */
    suspend fun deleteSession(sessionId: String): Result<String>
}

class SessionRemoteDataSourceImpl(
    private val sessionService: SessionService
) : SessionRemoteDataSource {

    override suspend fun getSession(sessionId: String): Result<Session> {
        return try {
            val dto = sessionService.get(sessionId)
            Bark.i("Fetched session (ID: $sessionId)")
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Bark.e("Failed to fetch session (ID: $sessionId). Serving cached data if available.", e)
            Result.failure(e)
        }
    }

    override suspend fun createSession(request: SessionCreateRequestDto): Result<Session> {
        return try {
            val response = sessionService.create(request)
            val session = response.session
                ?: throw Exception("Session creation succeeded but no session returned")
            Bark.i("Session created for club (ID: ${request.clubId})")
            Result.success(session.toDomain())
        } catch (e: Exception) {
            Bark.e("Failed to create session. Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun updateSession(request: SessionUpdateRequestDto): Result<Unit> {
        return try {
            val response = sessionService.update(request)
            if (response.success == false) {
                throw Exception("Update failed: ${response.message}")
            }
            Bark.i("Session updated (ID: ${request.id})")
            Result.success(Unit)
        } catch (e: Exception) {
            Bark.e("Failed to update session (ID: ${request.id}). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteSession(sessionId: String): Result<String> {
        return try {
            val response = sessionService.delete(sessionId)
            if (response.success) {
                Bark.i("Session deleted (ID: $sessionId)")
                Result.success(response.message)
            } else {
                Result.failure(Exception("Delete failed: ${response.message}"))
            }
        } catch (e: Exception) {
            Bark.e("Failed to delete session (ID: $sessionId). Please retry.", e)
            Result.failure(e)
        }
    }
}
