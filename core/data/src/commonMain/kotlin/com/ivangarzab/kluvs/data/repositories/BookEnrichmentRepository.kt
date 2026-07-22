package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.remote.source.BookEnrichmentRemoteDataSource
import com.ivangarzab.kluvs.model.BookEnrichment

/**
 * Repository for third-party book enrichment data (Google Books volume info, author bio/photo,
 * other books by the author) — kept separate from [BookRepository], which is scoped to Kluvs's
 * own book registry/search.
 *
 * Remote only, no local cache: enrichment data is display-only and not needed offline.
 */
interface BookEnrichmentRepository {

    /**
     * Fetches enrichment data for a book.
     *
     * @param externalGoogleId Google Books volume id
     * @param author Primary author name
     * @return Result containing the [BookEnrichment], or an error
     */
    suspend fun getEnrichment(externalGoogleId: String?, author: String?): Result<BookEnrichment>
}

internal class BookEnrichmentRepositoryImpl(
    private val bookEnrichmentRemoteDataSource: BookEnrichmentRemoteDataSource
) : BookEnrichmentRepository {

    override suspend fun getEnrichment(externalGoogleId: String?, author: String?): Result<BookEnrichment> {
        Bark.d("Getting book enrichment (externalGoogleId: $externalGoogleId, author: $author)")
        return bookEnrichmentRemoteDataSource.getEnrichment(externalGoogleId, author)
            .onSuccess {
                Bark.i("Book enrichment complete (externalGoogleId: $externalGoogleId, author: $author)")
            }.onFailure { error ->
                Bark.e("Book enrichment failed. Check network and retry.", error)
            }
    }
}
