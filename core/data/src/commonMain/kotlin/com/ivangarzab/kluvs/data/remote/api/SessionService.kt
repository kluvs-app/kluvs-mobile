package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DeleteResponseDto
import com.ivangarzab.kluvs.api.models.SessionCreateRequestDto
import com.ivangarzab.kluvs.api.models.SessionCreateResponseDto
import com.ivangarzab.kluvs.api.models.SessionDto
import com.ivangarzab.kluvs.api.models.SessionUpdateRequestDto
import com.ivangarzab.kluvs.api.models.UpdateSession200ResponseDto
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
    suspend fun create(request: SessionCreateRequestDto): SessionCreateResponseDto
    suspend fun update(request: SessionUpdateRequestDto): UpdateSession200ResponseDto
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

    override suspend fun update(request: SessionUpdateRequestDto): UpdateSession200ResponseDto {
        Bark.d("Updating session (ID: ${request.id})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("session") {
                method = HttpMethod.Put
                body = jsonString
            }.body<UpdateSession200ResponseDto>()
            Bark.v("Session updated successfully (ID: ${request.id})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to update session (ID: ${request.id}). Check network/API status and retry.", error)
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
