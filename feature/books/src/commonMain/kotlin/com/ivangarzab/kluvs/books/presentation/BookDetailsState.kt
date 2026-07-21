package com.ivangarzab.kluvs.books.presentation

import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.BookEnrichment
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus

/**
 * UI state for the book detail screen.
 */
data class BookDetailsState(
    val book: Book? = null,
    val isLoadingEnrichment: Boolean = true,
    val enrichment: BookEnrichment? = null,
    val shelfStatus: ShelfStatus? = null,
    val shelfSource: ShelfSource? = null,
    val isLiked: Boolean = false,
    val isMutationInProgress: Boolean = false,
    val operationError: String? = null
)
