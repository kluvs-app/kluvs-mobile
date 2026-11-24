package com.ivangarzab.kluvs.data.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.BookRepository
import com.ivangarzab.kluvs.model.BookSearchResult

/**
 * UseCase for searching books by a free-text query.
 *
 * Results come from the Google Books API via the backend and are not cached.
 *
 * @param bookRepository Repository for book data
 */
class SearchBooksUseCase(
    private val bookRepository: BookRepository
) {
    /**
     * Searches for books matching the given query.
     *
     * @param query Free-text search query
     * @param offset 0-based index of the first result to return, for pagination
     * @return Result containing the page of matching books plus the backend's total count, or an error
     */
    suspend operator fun invoke(query: String, offset: Int = 0): Result<BookSearchResult> {
        Bark.d("Searching books via use case (query: \"$query\", offset: $offset)")
        return bookRepository.searchBooks(query, offset)
    }
}
