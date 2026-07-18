package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.JoinPreviewResponseDto
import com.ivangarzab.kluvs.api.models.JoinRequestDto
import com.ivangarzab.kluvs.api.models.JoinResponseDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI

/**
 * Service for club invite links.
 *
 * [preview] validates a token without joining (no auth required by the backend).
 * [join] adds the authenticated member to the invite's club, so a user session
 * is required (bot callers are rejected).
 */
interface JoinService {
    suspend fun preview(token: String): JoinPreviewResponseDto
    suspend fun join(request: JoinRequestDto): JoinResponseDto
}

@OptIn(InternalAPI::class)
internal class JoinServiceImpl(private val supabase: SupabaseClient) : JoinService {

    override suspend fun preview(token: String): JoinPreviewResponseDto {
        Bark.d("Previewing club invite")
        return try {
            val response = supabase.functions.invoke("join") {
                method = HttpMethod.Get
                url {
                    parameters.append("token", token)
                }
            }.body<JoinPreviewResponseDto>()
            Bark.v("Invite preview fetched (valid: ${response.valid})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to preview club invite. Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun join(request: JoinRequestDto): JoinResponseDto {
        Bark.d("Joining club via invite")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("join") {
                method = HttpMethod.Post
                body = jsonString
            }.body<JoinResponseDto>()
            Bark.v("Joined club successfully (Club: ${response.clubId})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to join club via invite. Check network/API status and retry.", error)
            throw error
        }
    }
}
