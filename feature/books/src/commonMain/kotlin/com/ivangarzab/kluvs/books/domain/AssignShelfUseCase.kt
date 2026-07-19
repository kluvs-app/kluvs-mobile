package com.ivangarzab.kluvs.books.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.ShelfRepository
import com.ivangarzab.kluvs.model.ShelfStatus

/**
 * UseCase for assigning a book to a shelf.
 *
 * @param shelfRepository Repository for shelf data
 */
class AssignShelfUseCase(
    private val shelfRepository: ShelfRepository
) {
    suspend operator fun invoke(bookId: String, shelf: ShelfStatus): Result<ShelfStatus> {
        Bark.d("Assigning shelf via use case (book ID: $bookId, shelf: $shelf)")
        return shelfRepository.assignShelf(bookId, shelf)
    }
}
