package com.ivangarzab.kluvs.books.presentation

import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import com.ivangarzab.kluvs.presentation.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The purpose of this class is to wrap the [BookDetailsViewModel] for easier
 * use and access on the iOS side.
 */
@Suppress("unused")
class BookDetailsViewModelHelper : KoinComponent {

    private val viewModel: BookDetailsViewModel by inject()
    private val coroutineScope: CoroutineScope by inject()

    /**
     * iOS-friendly observation method.
     *
     * Returns a [Closeable] that can be used to cancel the observation.
     */
    fun observeState(callback: (BookDetailsState) -> Unit): Closeable {
        val job = viewModel.state.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    fun load(book: Book, shelfStatus: ShelfStatus?, shelfSource: ShelfSource?, isLiked: Boolean) =
        viewModel.load(book, shelfStatus, shelfSource, isLiked)
    fun onAssignShelf(shelf: ShelfStatus) = viewModel.onAssignShelf(shelf)
    fun onRemoveFromShelf() = viewModel.onRemoveFromShelf()
    fun onToggleLike() = viewModel.onToggleLike()
    fun onConsumeOperationError() = viewModel.onConsumeOperationError()
}
