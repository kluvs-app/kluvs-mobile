package com.ivangarzab.kluvs.books.presentation

import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfEntry

/**
 * UI state for the Books tab (shelf + search).
 */
data class BooksState(
    val isLoadingShelf: Boolean = true,
    val shelfError: String? = null,
    val shelfEntries: List<ShelfEntry> = emptyList(),
    val likedBookIds: Set<String> = emptySet(),
    val query: String = "",
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val searchResults: List<Book> = emptyList(),
    val isMutationInProgress: Boolean = false,
    val operationError: String? = null
)
