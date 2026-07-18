package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ProgressCreateRequestDto
import com.ivangarzab.kluvs.api.models.ProgressDeleteResponseDto
import com.ivangarzab.kluvs.api.models.ProgressUpdateRequestDto
import com.ivangarzab.kluvs.api.models.ReadingProgressDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI

/**
 * Service for the authenticated member's reading progress entries.
 *
 * All operations are member-scoped: the backend resolves the member from the
 * caller's auth token, so a user session is required (bot callers are rejected).
 *
 * Note: the backend returns raw [ReadingProgressDto] shapes (a bare array for GET,
 * the bare entity for POST/PUT) rather than the `{success, progress}` envelopes the
 * OpenAPI spec declares — a known backend/spec gap. This service decodes what the
 * backend actually sends.
 */
interface ProgressService {
    suspend fun getAll(
        bookId: Int? = null,
        sessionId: String? = null,
        status: String? = null,
    ): List<ReadingProgressDto>
    suspend fun create(request: ProgressCreateRequestDto): ReadingProgressDto
    suspend fun update(request: ProgressUpdateRequestDto): ReadingProgressDto
    suspend fun delete(progressId: String): ProgressDeleteResponseDto
}

@OptIn(InternalAPI::class)
internal class ProgressServiceImpl(private val supabase: SupabaseClient) : ProgressService {

    override suspend fun getAll(
        bookId: Int?,
        sessionId: String?,
        status: String?,
    ): List<ReadingProgressDto> {
        Bark.d("Fetching progress entries (Book: $bookId, Session: $sessionId, Status: $status)")
        return try {
            val response = supabase.functions.invoke("progress") {
                method = HttpMethod.Get
                url {
                    bookId?.let { parameters.append("book_id", it.toString()) }
                    sessionId?.let { parameters.append("session_id", it) }
                    status?.let { parameters.append("status", it) }
                }
            }.body<List<ReadingProgressDto>>()
            Bark.v("Progress entries fetched successfully (${response.size} entries)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch progress entries. Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun create(request: ProgressCreateRequestDto): ReadingProgressDto {
        Bark.d("Creating progress entry for book (ID: ${request.bookId})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("progress") {
                method = HttpMethod.Post
                body = jsonString
            }.body<ReadingProgressDto>()
            Bark.v("Progress entry created successfully (ID: ${response.id})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to create progress entry for book (ID: ${request.bookId}). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun update(request: ProgressUpdateRequestDto): ReadingProgressDto {
        Bark.d("Updating progress entry (ID: ${request.id})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("progress") {
                method = HttpMethod.Put
                body = jsonString
            }.body<ReadingProgressDto>()
            Bark.v("Progress entry updated successfully (ID: ${request.id})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to update progress entry (ID: ${request.id}). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun delete(progressId: String): ProgressDeleteResponseDto {
        Bark.d("Deleting progress entry (ID: $progressId)")
        return try {
            val response = supabase.functions.invoke("progress") {
                method = HttpMethod.Delete
                url {
                    parameters.append("id", progressId)
                }
            }.body<ProgressDeleteResponseDto>()
            Bark.v("Progress entry deleted successfully (ID: $progressId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to delete progress entry (ID: $progressId). Check network/API status and retry.", error)
            throw error
        }
    }
}
