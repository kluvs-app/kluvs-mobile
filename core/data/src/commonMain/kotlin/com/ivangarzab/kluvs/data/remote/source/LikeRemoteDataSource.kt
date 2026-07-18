package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.LikeToggleRequestDto
import com.ivangarzab.kluvs.data.remote.api.LikeService

/**
 * Remote data source for the authenticated member's book likes.
 *
 * Responsibilities:
 * - Calls [com.ivangarzab.kluvs.data.remote.api.LikeService] to fetch/toggle like state from Supabase
 * - Wraps results in [Result] for error handling
 */
interface LikeRemoteDataSource {

    /**
     * Fetches whether the member has liked the given book.
     */
    suspend fun getLikeStatus(bookId: Int): Result<Boolean>

    /**
     * Toggles the member's like on a book, returning the new liked state.
     */
    suspend fun toggleLike(request: LikeToggleRequestDto): Result<Boolean>
}

class LikeRemoteDataSourceImpl(
    private val likeService: LikeService
) : LikeRemoteDataSource {

    override suspend fun getLikeStatus(bookId: Int): Result<Boolean> {
        return try {
            val response = likeService.getStatus(bookId)
            val liked = response.liked
                ?: throw Exception("Like status response missing liked flag")
            Bark.d("Fetched like status for book (ID: $bookId, Liked: $liked)")
            Result.success(liked)
        } catch (e: Exception) {
            Bark.e("Failed to fetch like status for book (ID: $bookId). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun toggleLike(request: LikeToggleRequestDto): Result<Boolean> {
        return try {
            val response = likeService.toggle(request)
            val liked = response.liked
                ?: throw Exception("Like toggle response missing liked flag")
            Bark.i("Toggled like for book (ID: ${request.bookId}, Liked: $liked)")
            Result.success(liked)
        } catch (e: Exception) {
            Bark.e("Failed to toggle like for book (ID: ${request.bookId}). Please retry.", e)
            Result.failure(e)
        }
    }
}
