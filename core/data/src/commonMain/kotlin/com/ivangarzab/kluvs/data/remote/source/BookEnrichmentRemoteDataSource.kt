package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.remote.api.BookEnrichmentService
import com.ivangarzab.kluvs.data.remote.mappers.toDomain
import com.ivangarzab.kluvs.model.BookEnrichment

/**
 * Remote data source for book enrichment data (volume info, author info, other books by the author).
 *
 * Responsibilities:
 * - Calls [BookEnrichmentService] to fetch enrichment data from the `book-enrichment` proxy function
 * - Maps DTOs to domain models using mappers
 * - Wraps results in [Result] for error handling
 */
interface BookEnrichmentRemoteDataSource {

    /**
     * Fetches enrichment data for a book.
     *
     * @param externalGoogleId Google Books volume id, used to look up volume info and exclude
     * this book from [BookEnrichment.authorBooks]
     * @param author Primary author name, used to look up author info and other books by them
     * @return Result containing the [BookEnrichment], or an error
     */
    suspend fun getEnrichment(externalGoogleId: String?, author: String?): Result<BookEnrichment>
}

class BookEnrichmentRemoteDataSourceImpl(
    private val bookEnrichmentService: BookEnrichmentService
) : BookEnrichmentRemoteDataSource {

    override suspend fun getEnrichment(externalGoogleId: String?, author: String?): Result<BookEnrichment> {
        return try {
            val response = bookEnrichmentService.getEnrichment(externalGoogleId, author)
            val enrichment = response.toDomain(excludeExternalGoogleId = externalGoogleId)
            Bark.i("Book enrichment fetch complete (externalGoogleId: $externalGoogleId, author: $author)")
            Result.success(enrichment)
        } catch (e: Exception) {
            Bark.e("Failed to fetch book enrichment (externalGoogleId: $externalGoogleId, author: $author).", e)
            Result.failure(e)
        }
    }
}
