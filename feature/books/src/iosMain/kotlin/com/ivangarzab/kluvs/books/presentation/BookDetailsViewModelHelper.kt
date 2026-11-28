package com.ivangarzab.kluvs.books.presentation

import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModelHelper

/**
 * The purpose of this class is to wrap the [BookDetailsViewModel] for easier
 * use and access on the iOS side.
 */
@Suppress("unused")
class BookDetailsViewModelHelper : BaseViewModelHelper<BookDetailsState>() {

    override val viewModel: BookDetailsViewModel by viewModel()

    val isRegistered: Boolean get() = viewModel.isRegistered

    fun load(book: Book, shelfStatus: ShelfStatus?, shelfSource: ShelfSource?) =
        viewModel.load(book, shelfStatus, shelfSource)
    fun onAssignShelf(shelf: ShelfStatus) = viewModel.onAssignShelf(shelf)
    fun onRemoveFromShelf() = viewModel.onRemoveFromShelf()
    fun onToggleLike() = viewModel.onToggleLike()
}
