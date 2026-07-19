package com.ivangarzab.kluvs.books.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.LikeRepository

/**
 * UseCase for toggling the member's like on a book.
 *
 * @param likeRepository Repository for like data
 */
class ToggleLikeUseCase(
    private val likeRepository: LikeRepository
) {
    suspend operator fun invoke(bookId: String): Result<Boolean> {
        Bark.d("Toggling like via use case (book ID: $bookId)")
        return likeRepository.toggleLike(bookId)
    }
}
