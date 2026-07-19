package com.ivangarzab.kluvs.books.presentation

import com.ivangarzab.kluvs.model.ShelfStatus
import com.ivangarzab.kluvs.presentation.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The purpose of this class is to wrap the [BooksViewModel] for easier
 * use and access on the iOS side.
 */
@Suppress("unused")
class BooksViewModelHelper : KoinComponent {

    private val viewModel: BooksViewModel by inject()
    private val coroutineScope: CoroutineScope by inject()

    /**
     * iOS-friendly observation method.
     *
     * Returns a [Closeable] that can be used to cancel the observation.
     */
    fun observeState(callback: (BooksState) -> Unit): Closeable {
        val job = viewModel.state.onEach { callback(it) }.launchIn(coroutineScope)
        return Closeable { job.cancel() }
    }

    fun loadShelf(forceRefresh: Boolean) = viewModel.loadShelf(forceRefresh)
    fun onQueryChange(query: String) = viewModel.onQueryChange(query)
    fun search(query: String) = viewModel.search(query)
    fun onAssignShelf(bookId: String, shelf: ShelfStatus) = viewModel.onAssignShelf(bookId, shelf)
    fun onRemoveFromShelf(bookId: String) = viewModel.onRemoveFromShelf(bookId)
    fun onToggleLike(bookId: String) = viewModel.onToggleLike(bookId)
    fun onConsumeOperationError() = viewModel.onConsumeOperationError()
}
