package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DiscussionCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionDto
import com.ivangarzab.kluvs.api.models.DiscussionUpdateRequestDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI

/**
 * Service for discussion events tied to reading sessions.
 *
 * Mutations require the caller to be an owner/admin of the discussion's club.
 * Discussions are read through their parent session ([SessionService.get]);
 * there is no standalone GET endpoint.
 */
interface DiscussionService {
    suspend fun create(request: DiscussionCreateRequestDto): DiscussionDto
    suspend fun update(request: DiscussionUpdateRequestDto): DiscussionDto
    suspend fun delete(discussionId: String)
}

@OptIn(InternalAPI::class)
internal class DiscussionServiceImpl(private val supabase: SupabaseClient) : DiscussionService {

    override suspend fun create(request: DiscussionCreateRequestDto): DiscussionDto {
        Bark.d("Creating discussion for session (ID: ${request.sessionId})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("discussion") {
                method = HttpMethod.Post
                body = jsonString
            }.body<DiscussionDto>()
            Bark.v("Discussion created successfully (ID: ${response.id})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to create discussion for session (ID: ${request.sessionId}). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun update(request: DiscussionUpdateRequestDto): DiscussionDto {
        Bark.d("Updating discussion (ID: ${request.id})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("discussion") {
                method = HttpMethod.Put
                body = jsonString
            }.body<DiscussionDto>()
            Bark.v("Discussion updated successfully (ID: ${request.id})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to update discussion (ID: ${request.id}). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun delete(discussionId: String) {
        Bark.d("Deleting discussion (ID: $discussionId)")
        try {
            // Backend responds 204 No Content on success
            supabase.functions.invoke("discussion") {
                method = HttpMethod.Delete
                url {
                    parameters.append("id", discussionId)
                }
            }
            Bark.v("Discussion deleted successfully (ID: $discussionId)")
        } catch (error: Exception) {
            Bark.e("Failed to delete discussion (ID: $discussionId). Check network/API status and retry.", error)
            throw error
        }
    }
}
