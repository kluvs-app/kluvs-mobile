package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.LikeToggleRequestDto
import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.cache.CacheTTL
import com.ivangarzab.kluvs.data.local.source.LikeLocalDataSource
import com.ivangarzab.kluvs.data.remote.source.LikeRemoteDataSource

/**
 * Repository for the authenticated member's book likes.
 *
 * All operations are member-scoped: the backend resolves the member from the
 * caller's auth token, so a signed-in user session is required.
 */
interface LikeRepository {

    /**
     * Retrieves whether the member has liked the given book.
     *
     * @param bookId The ID of the book to look up
     * @return Result containing the liked state if successful
     */
    suspend fun isBookLiked(bookId: String): Result<Boolean>

    /**
     * Toggles the member's like on a book.
     *
     * @param bookId The ID of the book to toggle
     * @return Result containing the new liked state if successful
     */
    suspend fun toggleLike(bookId: String): Result<Boolean>
}

/**
 * Implementation of [LikeRepository] with TTL-based caching.
 *
 * Implements a cache-aside pattern:
 * - Reads check local cache first (1h TTL)
 * - Cache misses fetch from remote and populate cache
 * - Toggles write-through to cache on success
 */
internal class LikeRepositoryImpl(
    private val likeRemoteDataSource: LikeRemoteDataSource,
    private val likeLocalDataSource: LikeLocalDataSource,
    private val cachePolicy: CachePolicy
) : LikeRepository {

    override suspend fun isBookLiked(bookId: String): Result<Boolean> {
        val bookIdInt = bookId.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid book ID: $bookId"))

        val cachedLiked = likeLocalDataSource.getLikeStatus(bookId)
        val lastFetchedAt = likeLocalDataSource.getLastFetchedAt(bookId)

        if (cachedLiked != null && !cachePolicy.isStale(lastFetchedAt, CacheTTL.LIKE)) {
            Bark.d("Cache hit for like status (book ID: $bookId)")
            return Result.success(cachedLiked)
        }
        Bark.d("Cache miss for like status (book ID: $bookId)")

        val result = likeRemoteDataSource.getLikeStatus(bookIdInt)

        result.onSuccess { liked ->
            Bark.v("Persisting like status to cache (book ID: $bookId)")
            try {
                likeLocalDataSource.setLikeStatus(bookId, liked)
                Bark.d("Like status cached (book ID: $bookId)")
            } catch (e: Exception) {
                Bark.e("Like status cache failed. Will use remote data on next fetch.", e)
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch like status. Cached data may be unavailable.", error)
        }

        return result
    }

    override suspend fun toggleLike(bookId: String): Result<Boolean> {
        val bookIdInt = bookId.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid book ID: $bookId"))
        Bark.d("Toggling like for book (ID: $bookId)")
        val result = likeRemoteDataSource.toggleLike(LikeToggleRequestDto(bookId = bookIdInt))

        result.onSuccess { liked ->
            Bark.v("Persisting toggled like status to cache (book ID: $bookId)")
            try {
                likeLocalDataSource.setLikeStatus(bookId, liked)
                Bark.i("Like toggled and cached (book ID: $bookId)")
            } catch (e: Exception) {
                Bark.e("Like cache failed. Will fetch from remote if needed.", e)
            }
        }.onFailure { error ->
            Bark.e("Like toggle failed. Check input and retry.", error)
        }

        return result
    }
}
