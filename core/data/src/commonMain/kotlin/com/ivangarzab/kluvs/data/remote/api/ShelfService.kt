package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ShelfAssignRequestDto
import com.ivangarzab.kluvs.api.models.ShelfAssignResponseDto
import com.ivangarzab.kluvs.api.models.ShelfListResponseDto
import com.ivangarzab.kluvs.api.models.ShelfSingleResponseDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI

/**
 * Service for the authenticated member's book shelves.
 *
 * All operations are member-scoped: the backend resolves the member from the
 * caller's auth token, so a user session is required (bot callers are rejected).
 */
interface ShelfService {
    suspend fun getAll(): ShelfListResponseDto
    suspend fun getForBook(bookId: Int): ShelfSingleResponseDto
    suspend fun assign(request: ShelfAssignRequestDto): ShelfAssignResponseDto
    suspend fun remove(bookId: Int): ShelfSingleResponseDto
}

@OptIn(InternalAPI::class)
internal class ShelfServiceImpl(private val supabase: SupabaseClient) : ShelfService {

    override suspend fun getAll(): ShelfListResponseDto {
        Bark.d("Fetching all shelf entries")
        return try {
            val response = supabase.functions.invoke("shelf") {
                method = HttpMethod.Get
            }.body<ShelfListResponseDto>()
            Bark.v("Shelf entries fetched successfully (${response.shelves.size} entries)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch shelf entries. Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun getForBook(bookId: Int): ShelfSingleResponseDto {
        Bark.d("Fetching shelf for book (ID: $bookId)")
        return try {
            val response = supabase.functions.invoke("shelf") {
                method = HttpMethod.Get
                url {
                    parameters.append("book_id", bookId.toString())
                }
            }.body<ShelfSingleResponseDto>()
            Bark.v("Shelf fetched successfully for book (ID: $bookId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch shelf for book (ID: $bookId). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun assign(request: ShelfAssignRequestDto): ShelfAssignResponseDto {
        Bark.d("Assigning shelf for book (ID: ${request.bookId})")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("shelf") {
                method = HttpMethod.Post
                body = jsonString
            }.body<ShelfAssignResponseDto>()
            Bark.v("Shelf assigned successfully for book (ID: ${request.bookId})")
            response
        } catch (error: Exception) {
            Bark.e("Failed to assign shelf for book (ID: ${request.bookId}). Check network/API status and retry.", error)
            throw error
        }
    }

    override suspend fun remove(bookId: Int): ShelfSingleResponseDto {
        Bark.d("Removing shelf for book (ID: $bookId)")
        return try {
            val response = supabase.functions.invoke("shelf") {
                method = HttpMethod.Delete
                url {
                    parameters.append("book_id", bookId.toString())
                }
            }.body<ShelfSingleResponseDto>()
            Bark.v("Shelf removed successfully for book (ID: $bookId)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to remove shelf for book (ID: $bookId). Check network/API status and retry.", error)
            throw error
        }
    }
}
