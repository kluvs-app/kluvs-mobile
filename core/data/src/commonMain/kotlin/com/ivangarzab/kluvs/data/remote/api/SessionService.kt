package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DeleteResponseDto
import com.ivangarzab.kluvs.api.models.SessionCreateRequestDto
import com.ivangarzab.kluvs.api.models.SessionCreateResponseDto
import com.ivangarzab.kluvs.api.models.SessionDto
import com.ivangarzab.kluvs.api.models.SessionFinishResponseDto
import com.ivangarzab.kluvs.api.models.SessionNoChangesResponseDto
import com.ivangarzab.kluvs.api.models.SessionReadingLogResponseDto
import com.ivangarzab.kluvs.api.models.SessionUpdateRequestDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI

interface SessionService {
    /**
     * Fetches a single session by ID.
     *
     * GET /session is a two-mode endpoint (`oneOf` in the spec): without `id`, it
     * returns a reading-log digest; with `id` (what this method always sends), it
     * returns the full [SessionDto] shape — the same entity used for Club's
     * `active_session`.
     */
    suspend fun get(sessionId: String): SessionDto

    /**
     * Fetches the authenticated member's reading log — all sessions they participate
     * in, grouped by session status (active/finished). Member-scoped: requires a
     * user session (bot callers are rejected).
     */
    suspend fun getReadingLog(): SessionReadingLogResponseDto
    suspend fun create(request: SessionCreateRequestDto): SessionCreateResponseDto

    /**
     * Applies a general (non-finish) update.
     *
     * PUT /session is a `oneOf` in the spec (finish, general update or no-op), and the
     * generated merged `UpdateSession200ResponseDto` requires every variant's fields, so
     * it can't decode any single response. A general update or a no-op both carry
     * `success` and `message`, so this decodes just that common shape.
     */
    suspend fun update(request: SessionUpdateRequestDto): SessionNoChangesResponseDto

    /**
     * Finishes a session (`finish: true`). A successful finish always returns the finish
     * variant; an already-finished session is rejected with a 400.
     */
    suspend fun finish(sessionId: String): SessionFinishResponseDto
    suspend fun delete(sessionId: String): DeleteResponseDto
}

@OptIn(InternalAPI::class)
internal class SessionServiceImpl(private val supabase: SupabaseClient) : SessionService {

    override suspend fun get(sessionId: String): SessionDto {
        Bark.d("Fetching session (ID: $sessionId)")
        return try {
            val response = supabase.functions.invoke("session") {
                method = HttpMethod.Get
                url { parameters.append("id", sessionId) }
            }.body<SessionDto>()
            Bark.v("Session fetched successfully (ID: $sessionId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch session (ID: $sessionId). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun getReadingLog(): SessionReadingLogResponseDto {
        Bark.d("Fetching reading log")
        return try {
            val response = supabase.functions.invoke("session") {
                method = HttpMethod.Get
                url { parameters.append("reading_log", "true") }
            }.body<SessionReadingLogResponseDto>()
            Bark.v("Reading log fetched successfully")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch reading log. Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun create(request: SessionCreateRequestDto): SessionCreateResponseDto {
        Bark.d("Creating session")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("session") {
                method = HttpMethod.Post
                body = jsonString
            }.body<SessionCreateResponseDto>()
            Bark.v("Session created successfully")
            response
        } catch (error: Exception) {
            Bark.e("Failed to create session. Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun update(request: SessionUpdateRequestDto): SessionNoChangesResponseDto {
        Bark.d("Updating session (ID: ${request.id})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("session") {
                method = HttpMethod.Put
                body = jsonString
            }.body<SessionNoChangesResponseDto>()
            Bark.v("Session updated successfully (ID: ${request.id})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to update session (ID: ${request.id}). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun finish(sessionId: String): SessionFinishResponseDto {
        Bark.d("Finishing session (ID: $sessionId)")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(SessionUpdateRequestDto(id = sessionId, finish = true))

            val response = supabase.functions.invoke("session") {
                method = HttpMethod.Put
                body = jsonString
            }.body<SessionFinishResponseDto>()
            Bark.v("Session finished successfully (ID: $sessionId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to finish session (ID: $sessionId). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun delete(sessionId: String): DeleteResponseDto {
        Bark.d("Deleting session (ID: $sessionId)")
        return try {
            val response = supabase.functions.invoke("session") {
                method = HttpMethod.Delete
                url { parameters.append("id", sessionId) }
            }.body<DeleteResponseDto>()
            Bark.v("Session deleted successfully (ID: $sessionId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to delete session (ID: $sessionId). Check network/API status and retry.", error)
            throw error
        }
    }
}
