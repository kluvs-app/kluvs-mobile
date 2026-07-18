package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.LikeToggleRequestDto
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

internal class LikeRepositoryImpl(
    private val likeRemoteDataSource: LikeRemoteDataSource
) : LikeRepository {

    override suspend fun isBookLiked(bookId: String): Result<Boolean> {
        val bookIdInt = bookId.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid book ID: $bookId"))
        return likeRemoteDataSource.getLikeStatus(bookIdInt)
    }

    override suspend fun toggleLike(bookId: String): Result<Boolean> {
        val bookIdInt = bookId.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid book ID: $bookId"))
        Bark.d("Toggling like for book (ID: $bookId)")
        return likeRemoteDataSource.toggleLike(LikeToggleRequestDto(bookId = bookIdInt))
    }
}
