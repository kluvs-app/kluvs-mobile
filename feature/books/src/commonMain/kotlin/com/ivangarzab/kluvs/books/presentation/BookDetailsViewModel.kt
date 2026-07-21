package com.ivangarzab.kluvs.books.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.books.domain.AssignShelfUseCase
import com.ivangarzab.kluvs.books.domain.GetBookEnrichmentUseCase
import com.ivangarzab.kluvs.books.domain.GetLikeStatusUseCase
import com.ivangarzab.kluvs.books.domain.RemoveFromShelfUseCase
import com.ivangarzab.kluvs.books.domain.ToggleLikeUseCase
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The purpose of this [ViewModel] class is to serve the book detail screen: volume info,
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
    private val toggleLike: ToggleLikeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BookDetailsState())
    val state: StateFlow<BookDetailsState> = _state.asStateFlow()

    /** Registered books have a server-assigned numeric [Book.id]; others can't be shelved/liked yet. */
    val isRegistered: Boolean
        get() = _state.value.book?.id?.toIntOrNull() != null

    fun load(book: Book, shelfStatus: ShelfStatus? = null, shelfSource: ShelfSource? = null) {
        _state.update {
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
                    _state.update { it.copy(isLoadingEnrichment = false, enrichment = enrichment) }
                }
                .onFailure { error ->
                    Bark.e("Failed to load book enrichment (book ID: ${book.id}). Degrading gracefully.", error)
                    _state.update { it.copy(isLoadingEnrichment = false, enrichment = null) }
                }
        }

        if (isRegistered) {
            viewModelScope.launch {
                getLikeStatus(book.id)
                    .onSuccess { liked ->
                        Bark.i("Loaded like status (book ID: ${book.id}, liked: $liked)")
                        _state.update { it.copy(isLiked = liked) }
                    }
                    .onFailure { error ->
                        Bark.e("Failed to load like status (book ID: ${book.id}). Defaulting to unliked.", error)
                    }
            }
        }
    }

    fun onAssignShelf(shelf: ShelfStatus) {
        val bookId = _state.value.book?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isMutationInProgress = true) }

            assignShelf(bookId, shelf)
                .onSuccess { newShelf ->
                    Bark.i("Assigned book (ID: $bookId) to shelf: $newShelf")
                    _state.update {
                        it.copy(
                            isMutationInProgress = false,
                            shelfStatus = newShelf,
                            shelfSource = ShelfSource.MANUAL
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Failed to assign shelf (book ID: $bookId). ${error.message}", error)
                    _state.update {
                        it.copy(
                            isMutationInProgress = false,
                            operationError = error.message ?: "Failed to update shelf"
                        )
                    }
                }
        }
    }

    fun onRemoveFromShelf() {
        val bookId = _state.value.book?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isMutationInProgress = true) }

            removeFromShelf(bookId)
                .onSuccess {
                    Bark.i("Removed book (ID: $bookId) from shelf")
                    _state.update {
                        it.copy(isMutationInProgress = false, shelfStatus = null, shelfSource = null)
                    }
                }
                .onFailure { error ->
                    Bark.e("Failed to remove book from shelf (book ID: $bookId). ${error.message}", error)
                    _state.update {
                        it.copy(
                            isMutationInProgress = false,
                            operationError = error.message ?: "Failed to update shelf"
                        )
                    }
                }
        }
    }

    fun onToggleLike() {
        val bookId = _state.value.book?.id ?: return
        viewModelScope.launch {
            toggleLike(bookId)
                .onSuccess { liked ->
                    Bark.i("Toggled like (book ID: $bookId, liked: $liked)")
                    _state.update { it.copy(isLiked = liked) }
                }
                .onFailure { error ->
                    Bark.e("Failed to toggle like (book ID: $bookId). ${error.message}", error)
                    _state.update {
                        it.copy(operationError = error.message ?: "Failed to update like")
                    }
                }
        }
    }

    fun onConsumeOperationError() {
        _state.update { it.copy(operationError = null) }
    }
}
