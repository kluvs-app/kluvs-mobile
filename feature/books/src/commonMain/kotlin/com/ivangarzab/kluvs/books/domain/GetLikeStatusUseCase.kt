package com.ivangarzab.kluvs.books.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.LikeRepository

/**
 * UseCase for fetching whether the authenticated member has liked a book.
 *
 * @param likeRepository Repository for like data
 */
class GetLikeStatusUseCase(
    private val likeRepository: LikeRepository
) {
    suspend operator fun invoke(bookId: String): Result<Boolean> {
        Bark.d("Fetching like status via use case (book ID: $bookId)")
        return likeRepository.isBookLiked(bookId)
    }
}
