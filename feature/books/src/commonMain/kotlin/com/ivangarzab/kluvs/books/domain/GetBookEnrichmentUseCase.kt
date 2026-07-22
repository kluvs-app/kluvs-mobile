package com.ivangarzab.kluvs.books.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.BookEnrichmentRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.BookEnrichment

/**
 * UseCase for fetching book enrichment data (volume info, author info, other books by the author).
 *
 * @param bookEnrichmentRepository Repository for book enrichment data
 */
class GetBookEnrichmentUseCase(
    private val bookEnrichmentRepository: BookEnrichmentRepository
) {
    suspend operator fun invoke(book: Book): Result<BookEnrichment> {
        val primaryAuthor = book.author
            .split(Regex("\\s*(?:,|&| and )\\s*", RegexOption.IGNORE_CASE))
            .firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        Bark.d("Getting book enrichment via use case (book ID: ${book.id}, primary author: $primaryAuthor)")
        return bookEnrichmentRepository.getEnrichment(book.externalGoogleId, primaryAuthor)
    }
}
