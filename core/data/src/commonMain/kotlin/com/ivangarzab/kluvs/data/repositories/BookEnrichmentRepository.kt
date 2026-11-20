package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.cache.CacheTTL
import com.ivangarzab.kluvs.data.local.cache.recoverWithCache
import com.ivangarzab.kluvs.data.local.mappers.bookEnrichmentCacheKey
import com.ivangarzab.kluvs.data.local.source.BookEnrichmentLocalDataSource
import com.ivangarzab.kluvs.data.remote.source.BookEnrichmentRemoteDataSource
import com.ivangarzab.kluvs.model.BookEnrichment

/**
 * Repository for third-party book enrichment data (Google Books volume info, author bio/photo,
 * other books by the author) — kept separate from [BookRepository], which is scoped to Kluvs's
 * own book registry/search.
 *
 * Cache-first via Room so the book details screen still renders these sections offline: a
 * fresh cached response (within [CacheTTL.BOOK_ENRICHMENT]) is served without a request; a
 * missing or stale one is refetched and written back; and if that fetch fails, the stale
 * cached response is served instead.
 */
interface BookEnrichmentRepository {

    /**
     * Gets enrichment data for a book.
     *
     * @param externalGoogleId Google Books volume id
     * @param author Primary author name
     * @param forceRefresh Skip the cache-freshness check and fetch remotely (cache is still the
     * fallback if the fetch fails)
     * @return Result containing the [BookEnrichment], or an error when the fetch failed and
     * nothing is cached
     */
    suspend fun getEnrichment(
        externalGoogleId: String?,
        author: String?,
        forceRefresh: Boolean = false
    ): Result<BookEnrichment>
}

internal class BookEnrichmentRepositoryImpl(
    private val bookEnrichmentRemoteDataSource: BookEnrichmentRemoteDataSource,
    private val bookEnrichmentLocalDataSource: BookEnrichmentLocalDataSource,
    private val cachePolicy: CachePolicy
) : BookEnrichmentRepository {

    override suspend fun getEnrichment(
        externalGoogleId: String?,
        author: String?,
        forceRefresh: Boolean
    ): Result<BookEnrichment> {
        val key = bookEnrichmentCacheKey(externalGoogleId, author)
        Bark.d("Getting book enrichment (externalGoogleId: $externalGoogleId, author: $author)")

        // 1. Check local cache (unless force refresh)
        if (!forceRefresh) {
            try {
                val cached = bookEnrichmentLocalDataSource.getEnrichment(key)
                val lastFetchedAt = bookEnrichmentLocalDataSource.getLastFetchedAt(key)
                if (cached != null && cachePolicy.isFresh(lastFetchedAt, CacheTTL.BOOK_ENRICHMENT)) {
                    Bark.d("Cache hit for book enrichment (key: $key)")
                    return Result.success(cached)
                }
            } catch (e: Exception) {
                Bark.e("Failed to read cached book enrichment (key: $key). Fetching remotely.", e)
            }
            Bark.d("Cache miss for book enrichment (key: $key)")
        }

        // 2. Fetch from remote
        val result = bookEnrichmentRemoteDataSource.getEnrichment(externalGoogleId, author)

        // 3. Cache on success
        result.onSuccess { enrichment ->
            try {
                bookEnrichmentLocalDataSource.insertEnrichment(key, enrichment)
                Bark.i("Book enrichment fetched and cached (key: $key)")
            } catch (e: Exception) {
                Bark.e("Failed to cache book enrichment (key: $key). Retry on next fetch.", e)
            }
        }

        // 4. On failure, fall back to the cached row even if stale
        return result.recoverWithCache("book enrichment (key: $key)") {
            bookEnrichmentLocalDataSource.getEnrichment(key)
        }
    }
}
