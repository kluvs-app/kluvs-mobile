package com.ivangarzab.kluvs.books.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.ShelfRepository
import com.ivangarzab.kluvs.model.ShelfEntry

/**
 * UseCase for fetching the authenticated member's book shelf.
 *
 * @param shelfRepository Repository for shelf data
 */
class GetShelfUseCase(
    private val shelfRepository: ShelfRepository
) {
    suspend operator fun invoke(): Result<List<ShelfEntry>> {
        Bark.d("Fetching shelf via use case")
        return shelfRepository.getShelf()
    }
}
