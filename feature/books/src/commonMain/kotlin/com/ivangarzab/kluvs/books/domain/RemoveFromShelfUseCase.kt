package com.ivangarzab.kluvs.books.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.ShelfRepository

/**
 * UseCase for removing a book from the member's shelf.
 *
 * @param shelfRepository Repository for shelf data
 */
class RemoveFromShelfUseCase(
    private val shelfRepository: ShelfRepository
) {
    suspend operator fun invoke(bookId: String): Result<Unit> {
        Bark.d("Removing book from shelf via use case (book ID: $bookId)")
        return shelfRepository.removeFromShelf(bookId)
    }
}
