package com.ivangarzab.kluvs.data.remote.api

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.BookEnrichmentResponseDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.HttpMethod
import io.ktor.utils.io.InternalAPI

interface BookEnrichmentService {
    /** Fetches enrichment data (volume info, author info, other books by the author). */
    suspend fun getEnrichment(externalGoogleId: String?, author: String?): BookEnrichmentResponseDto
}

@OptIn(InternalAPI::class)
internal class BookEnrichmentServiceImpl(private val supabase: SupabaseClient) : BookEnrichmentService {

    override suspend fun getEnrichment(externalGoogleId: String?, author: String?): BookEnrichmentResponseDto {
        Bark.d("Fetching book enrichment (externalGoogleId: $externalGoogleId, author: $author)")
        return try {
            val response = supabase.functions.invoke("book-enrichment") {
                method = HttpMethod.Get
                url {
                    if (externalGoogleId != null) parameters.append("external_google_id", externalGoogleId)
                    if (author != null) parameters.append("author", author)
                }
            }.body<BookEnrichmentResponseDto>()
            Bark.v("Book enrichment fetch succeeded (externalGoogleId: $externalGoogleId, author: $author)")
            response
        } catch (error: Exception) {
            Bark.e("Failed to fetch book enrichment (externalGoogleId: $externalGoogleId, author: $author). Check network/API status.", error)
            throw error
        }
    }
}
