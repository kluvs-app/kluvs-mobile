package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.BookLookupResponseDto
import com.ivangarzab.kluvs.api.models.BookRegistrationRequestDto
import com.ivangarzab.kluvs.api.models.BookRegistrationResponseDto
import com.ivangarzab.kluvs.api.models.BookSearchResponseDto
import com.ivangarzab.kluvs.network.utils.JsonHelper.getJsonForSupabaseService
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI

interface BookService {
    /** Search books by query string. Returns up to [limit] results (null = server default). */
    suspend fun search(query: String, limit: Int? = null): BookSearchResponseDto

    /** Look up a single book by ISBN. */
    suspend fun lookupByIsbn(isbn: String): BookLookupResponseDto

    /** Register a book (create if not exists, or return existing). */
    suspend fun register(request: BookRegistrationRequestDto): BookRegistrationResponseDto
}

@OptIn(InternalAPI::class)
internal class BookServiceImpl(private val supabase: SupabaseClient) : BookService {

    override suspend fun search(query: String, limit: Int?): BookSearchResponseDto {
        Bark.d("Searching books (query: \"$query\")")
        return try {
            val response = supabase.functions.invoke("book") {
                method = HttpMethod.Get
                url {
                    parameters.append("q", query)
                    if (limit != null) parameters.append("limit", limit.toString())
                }
            }.body<BookSearchResponseDto>()
            Bark.v("Book search returned ${response.books?.size ?: 0} results")
            response
        } catch (error: Exception) {
            Bark.e("Failed to search books (query: \"$query\"). Check network/API status.", error)
            throw error
        }
    }

    override suspend fun lookupByIsbn(isbn: String): BookLookupResponseDto {
        Bark.d("Looking up book by ISBN: $isbn")
        return try {
            val response = supabase.functions.invoke("book") {
                method = HttpMethod.Get
                url { parameters.append("isbn", isbn) }
            }.body<BookLookupResponseDto>()
            Bark.v("Book lookup by ISBN succeeded (ISBN: $isbn)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to look up book by ISBN ($isbn). Check network/API status.", error)
            throw error
        }
    }

    override suspend fun register(request: BookRegistrationRequestDto): BookRegistrationResponseDto {
        Bark.d("Registering book: ${request.title}")
        return try {
            val json = getJsonForSupabaseService()
            val jsonString = json.encodeToString(request)

            val response = supabase.functions.invoke("book") {
                method = HttpMethod.Post
                body = jsonString
            }.body<BookRegistrationResponseDto>()
            Bark.v("Book registered (created=${response.created}): ${request.title}")
            response
        } catch (error: Exception) {
            Bark.e("Failed to register book (${request.title}). Check network/API status.", error)
            throw error
        }
    }
}
