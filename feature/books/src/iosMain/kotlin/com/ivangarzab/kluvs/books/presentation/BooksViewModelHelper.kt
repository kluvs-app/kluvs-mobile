package com.ivangarzab.kluvs.books.presentation

import com.ivangarzab.kluvs.model.ShelfStatus
import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModelHelper

/**
 * The purpose of this class is to wrap the [BooksViewModel] for easier
 * use and access on the iOS side.
 */
@Suppress("unused")
class BooksViewModelHelper : BaseViewModelHelper<BooksState>() {

    override val viewModel: BooksViewModel by viewModel()

    fun loadShelf(forceRefresh: Boolean) = viewModel.loadShelf(forceRefresh)
    fun onQueryChange(query: String) = viewModel.onQueryChange(query)
    fun search(query: String) = viewModel.search(query)
    fun loadMoreSearchResults() = viewModel.loadMoreSearchResults()
    fun onAssignShelf(bookId: String, shelf: ShelfStatus) = viewModel.onAssignShelf(bookId, shelf)
    fun onRemoveFromShelf(bookId: String) = viewModel.onRemoveFromShelf(bookId)
    fun onToggleLike(bookId: String) = viewModel.onToggleLike(bookId)
}
