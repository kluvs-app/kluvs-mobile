package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DiscussionNoteCreateRequestDto
import com.ivangarzab.kluvs.api.models.DiscussionNoteDto
import com.ivangarzab.kluvs.api.models.DiscussionNoteUpdateRequestDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI

/**
 * Service for the authenticated member's private discussion notes.
 *
 * All operations are member-scoped: the backend resolves the member from the
 * caller's auth token, so a user session is required (bot callers are rejected).
 * Each member can hold at most one note per discussion (POST returns 409 on duplicates).
 */
interface DiscussionNoteService {
    /** Returns the caller's note for the given discussion, or null if none exists. */
    suspend fun get(discussionId: String): DiscussionNoteDto?
    suspend fun create(request: DiscussionNoteCreateRequestDto): DiscussionNoteDto
    suspend fun update(request: DiscussionNoteUpdateRequestDto): DiscussionNoteDto
    suspend fun delete(noteId: String)
}

@OptIn(InternalAPI::class)
internal class DiscussionNoteServiceImpl(private val supabase: SupabaseClient) : DiscussionNoteService {

    override suspend fun get(discussionId: String): DiscussionNoteDto? {
        Bark.d("Fetching note for discussion (ID: $discussionId)")
        return try {
            // Backend returns a JSON `null` body (200) when the caller has no note
            val bodyText = supabase.functions.invoke("discussion-note") {
                method = HttpMethod.Get
                url {
                    parameters.append("discussion_id", discussionId)
                }
            }.bodyAsText()

            val response = getJsonForSupabaseService()
                .decodeFromString<DiscussionNoteDto?>(bodyText)
            Bark.v("Note fetched for discussion (ID: $discussionId, exists: ${response != null})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch note for discussion (ID: $discussionId). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun create(request: DiscussionNoteCreateRequestDto): DiscussionNoteDto {
        Bark.d("Creating note for discussion (ID: ${request.discussionId})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("discussion-note") {
                method = HttpMethod.Post
                body = jsonString
            }.body<DiscussionNoteDto>()
            Bark.v("Note created successfully (ID: ${response.id})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to create note for discussion (ID: ${request.discussionId}). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun update(request: DiscussionNoteUpdateRequestDto): DiscussionNoteDto {
        Bark.d("Updating note (ID: ${request.id})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("discussion-note") {
                method = HttpMethod.Put
                body = jsonString
            }.body<DiscussionNoteDto>()
            Bark.v("Note updated successfully (ID: ${request.id})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to update note (ID: ${request.id}). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun delete(noteId: String) {
        Bark.d("Deleting note (ID: $noteId)")
        try {
            // Backend responds 204 No Content on success
            supabase.functions.invoke("discussion-note") {
                method = HttpMethod.Delete
                url {
                    parameters.append("id", noteId)
                }
            }
            Bark.v("Note deleted successfully (ID: $noteId)")
        } catch (error: Exception) {
            Bark.e("Failed to delete note (ID: $noteId). Check network/API status and retry.", error)
            throw error
        }
    }
}
