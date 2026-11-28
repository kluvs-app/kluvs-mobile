package com.ivangarzab.kluvs.books.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.books.domain.AssignShelfUseCase
import com.ivangarzab.kluvs.books.domain.GetBookEnrichmentUseCase
import com.ivangarzab.kluvs.books.domain.GetLikeStatusUseCase
import com.ivangarzab.kluvs.data.domain.RegisterBookUseCase
import com.ivangarzab.kluvs.books.domain.RemoveFromShelfUseCase
import com.ivangarzab.kluvs.books.domain.ToggleLikeUseCase
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import com.ivangarzab.kluvs.presentation.error.toDisplayMessage
import com.ivangarzab.kluvs.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

/**
 * The purpose of this ViewModel is to serve the book detail screen: volume info,
 * author enrichment, "more by this author", and shelf/like controls for a single [Book].
 *
 * Dedicated from [BooksViewModel] since detail can be entered from the shelf, search, or
 * recursively from another detail screen's "more by this author" row, and its state doesn't
 * overlap with the shelf/search list.
 */
class BookDetailsViewModel(
    private val getBookEnrichment: GetBookEnrichmentUseCase,
    private val getLikeStatus: GetLikeStatusUseCase,
    private val assignShelf: AssignShelfUseCase,
    private val removeFromShelf: RemoveFromShelfUseCase,
    private val toggleLike: ToggleLikeUseCase,
    private val registerBook: RegisterBookUseCase
) : BaseViewModel<BookDetailsState>(BookDetailsState()) {

    /** Registered books have a server-assigned numeric [Book.id]; others can't be shelved/liked yet. */
    val isRegistered: Boolean
        get() = currentState.book?.id?.toIntOrNull() != null

    fun load(book: Book, shelfStatus: ShelfStatus? = null, shelfSource: ShelfSource? = null) {
        updateState {
            BookDetailsState(
                book = book,
                isLoadingEnrichment = true,
                shelfStatus = shelfStatus,
                shelfSource = shelfSource
            )
        }

        viewModelScope.launch {
            getBookEnrichment(book)
                .onSuccess { enrichment ->
                    Bark.i("Loaded book enrichment (book ID: ${book.id})")
                    updateState { it.copy(isLoadingEnrichment = false, enrichment = enrichment) }
                }
                .onFailure { error ->
                    Bark.e("Failed to load book enrichment (book ID: ${book.id}). Degrading gracefully.", error)
                    updateState { it.copy(isLoadingEnrichment = false, enrichment = null) }
                }
        }

        if (isRegistered) {
            loadLikeStatus(book.id)
        } else {
            viewModelScope.launch {
                registerBook(book)
                    .onSuccess { registered ->
                        Bark.i("Registered book from detail screen (ID: ${registered.id})")
                        updateState { it.copy(book = registered) }
                        loadLikeStatus(registered.id)
                    }
                    .onFailure { error ->
                        Bark.e("Failed to register book (title: ${book.title}). Shelf/like actions unavailable.", error)
                    }
            }
        }
    }

    private fun loadLikeStatus(bookId: String) {
        viewModelScope.launch {
            getLikeStatus(bookId)
                .onSuccess { liked ->
                    Bark.i("Loaded like status (book ID: $bookId, liked: $liked)")
                    updateState { it.copy(isLiked = liked) }
                }
                .onFailure { error ->
                    Bark.e("Failed to load like status (book ID: $bookId). Defaulting to unliked.", error)
                }
        }
    }

    fun onAssignShelf(shelf: ShelfStatus) {
        val bookId = currentState.book?.id ?: return
        viewModelScope.launch {
            updateState { it.copy(isMutationInProgress = true) }

            assignShelf(bookId, shelf)
                .onSuccess { newShelf ->
                    Bark.i("Assigned book (ID: $bookId) to shelf: $newShelf")
                    updateState {
                        it.copy(
                            isMutationInProgress = false,
                            shelfStatus = newShelf,
                            shelfSource = ShelfSource.MANUAL
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Failed to assign shelf (book ID: $bookId). ${error.message}", error)
                    updateState { it.copy(isMutationInProgress = false) }
                    send(ScreenEffect.ShowSnackbar.error(error.toDisplayMessage()))
                }
        }
    }

    fun onRemoveFromShelf() {
        val bookId = currentState.book?.id ?: return
        viewModelScope.launch {
            updateState { it.copy(isMutationInProgress = true) }

            removeFromShelf(bookId)
                .onSuccess {
                    Bark.i("Removed book (ID: $bookId) from shelf")
                    updateState {
                        it.copy(isMutationInProgress = false, shelfStatus = null, shelfSource = null)
                    }
                }
                .onFailure { error ->
                    Bark.e("Failed to remove book from shelf (book ID: $bookId). ${error.message}", error)
                    updateState { it.copy(isMutationInProgress = false) }
                    send(ScreenEffect.ShowSnackbar.error(error.toDisplayMessage()))
                }
        }
    }

    fun onToggleLike() {
        val bookId = currentState.book?.id ?: return
        viewModelScope.launch {
            toggleLike(bookId)
                .onSuccess { liked ->
                    Bark.i("Toggled like (book ID: $bookId, liked: $liked)")
                    updateState { it.copy(isLiked = liked) }
                }
                .onFailure { error ->
                    Bark.e("Failed to toggle like (book ID: $bookId). ${error.message}", error)
                    send(ScreenEffect.ShowSnackbar.error(error.toDisplayMessage()))
                }
        }
    }
}
