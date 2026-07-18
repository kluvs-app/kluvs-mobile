package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceRosterResponseDto
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceUpsertRequestDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI

/**
 * Service for discussion RSVP (attendance) responses.
 *
 * All operations are member-scoped: the backend resolves the member from the
 * caller's auth token, so a user session is required (bot callers are rejected).
 */
interface DiscussionAttendanceService {
    suspend fun getRoster(discussionId: String): DiscussionAttendanceRosterResponseDto
    suspend fun upsert(request: DiscussionAttendanceUpsertRequestDto): DiscussionAttendanceDto
    /** Clears the caller's RSVP for the given discussion (back to unanswered). */
    suspend fun clear(discussionId: String)
}

@OptIn(InternalAPI::class)
internal class DiscussionAttendanceServiceImpl(
    private val supabase: SupabaseClient,
) : DiscussionAttendanceService {

    override suspend fun getRoster(discussionId: String): DiscussionAttendanceRosterResponseDto {
        Bark.d("Fetching attendance roster for discussion (ID: $discussionId)")
        return try {
            val response = supabase.functions.invoke("discussion-attendance") {
                method = HttpMethod.Get
                url {
                    parameters.append("discussion_id", discussionId)
                }
            }.body<DiscussionAttendanceRosterResponseDto>()
            Bark.v("Attendance roster fetched for discussion (ID: $discussionId, responses: ${response.responses.size})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch attendance roster for discussion (ID: $discussionId). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun upsert(request: DiscussionAttendanceUpsertRequestDto): DiscussionAttendanceDto {
        Bark.d("Upserting RSVP for discussion (ID: ${request.discussionId}, Status: ${request.status})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("discussion-attendance") {
                method = HttpMethod.Post
                body = jsonString
            }.body<DiscussionAttendanceDto>()
            Bark.v("RSVP upserted successfully for discussion (ID: ${request.discussionId})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to upsert RSVP for discussion (ID: ${request.discussionId}). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun clear(discussionId: String) {
        Bark.d("Clearing RSVP for discussion (ID: $discussionId)")
        try {
            // Backend responds 204 No Content on success
            supabase.functions.invoke("discussion-attendance") {
                method = HttpMethod.Delete
                url {
                    parameters.append("discussion_id", discussionId)
                }
            }
            Bark.v("RSVP cleared successfully for discussion (ID: $discussionId)")
        } catch (error: Exception) {
            Bark.e("Failed to clear RSVP for discussion (ID: $discussionId). Check network/API status and retry.", error)
            throw error
        }
    }
}
