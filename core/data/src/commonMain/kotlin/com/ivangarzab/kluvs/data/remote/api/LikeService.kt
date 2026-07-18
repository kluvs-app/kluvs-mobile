package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.LikeStatusResponseDto
import com.ivangarzab.kluvs.api.models.LikeToggleRequestDto
import com.ivangarzab.kluvs.api.models.LikeToggleResponseDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI

/**
 * Service for the authenticated member's book likes.
 *
 * All operations are member-scoped: the backend resolves the member from the
 * caller's auth token, so a user session is required (bot callers are rejected).
 */
interface LikeService {
    suspend fun getStatus(bookId: Int): LikeStatusResponseDto
    suspend fun toggle(request: LikeToggleRequestDto): LikeToggleResponseDto
}

@OptIn(InternalAPI::class)
internal class LikeServiceImpl(private val supabase: SupabaseClient) : LikeService {

    override suspend fun getStatus(bookId: Int): LikeStatusResponseDto {
        Bark.d("Fetching like status for book (ID: $bookId)")
        return try {
            val response = supabase.functions.invoke("like") {
                method = HttpMethod.Get
                url {
                    parameters.append("book_id", bookId.toString())
                }
            }.body<LikeStatusResponseDto>()
            Bark.v("Like status fetched successfully for book (ID: $bookId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch like status for book (ID: $bookId). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun toggle(request: LikeToggleRequestDto): LikeToggleResponseDto {
        Bark.d("Toggling like for book (ID: ${request.bookId})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("like") {
                method = HttpMethod.Post
                body = jsonString
            }.body<LikeToggleResponseDto>()
            Bark.v("Like toggled successfully for book (ID: ${request.bookId})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to toggle like for book (ID: ${request.bookId}). Check network/API status and retry.", error)
            throw error
        }
    }
}
