package com.ivangarzab.kluvs.books.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.books.domain.AssignShelfUseCase
import com.ivangarzab.kluvs.books.domain.GetShelfUseCase
import com.ivangarzab.kluvs.books.domain.RemoveFromShelfUseCase
import com.ivangarzab.kluvs.books.domain.SearchBooksUseCase
import com.ivangarzab.kluvs.books.domain.ToggleLikeUseCase
import com.ivangarzab.kluvs.data.error.toAppError
import com.ivangarzab.kluvs.presentation.error.toDisplayMessage
import com.ivangarzab.kluvs.presentation.error.toUserMessage
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The purpose of this [ViewModel] class is to serve the Books tab (shelf + search).
 */
class BooksViewModel(
    private val getShelf: GetShelfUseCase,
    private val assignShelf: AssignShelfUseCase,
    private val removeFromShelf: RemoveFromShelfUseCase,
    private val toggleLike: ToggleLikeUseCase,
    private val searchBooks: SearchBooksUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BooksState())
    val state: StateFlow<BooksState> = _state.asStateFlow()

    fun loadShelf(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingShelf = true, shelfError = null) }

            getShelf(forceRefresh)
                .onSuccess { entries ->
                    Bark.i("Loaded shelf (${entries.size} entries)")
                    _state.update { it.copy(isLoadingShelf = false, shelfEntries = entries) }
                }
                .onFailure { error ->
                    Bark.e("Failed to load shelf. Please retry.", error)
                    _state.update {
                        it.copy(
                            isLoadingShelf = false,
                            shelfError = error.toAppError().toUserMessage()
                        )
                    }
                }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _state.update {
                it.copy(searchResults = emptyList(), searchTotal = 0, searchError = null, isSearching = false)
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, searchError = null) }

            searchBooks(query)
                .onSuccess { result ->
                    Bark.i("Book search complete (${result.books.size} results, total: ${result.total})")
                    _state.update {
                        it.copy(
                            isSearching = false,
                            searchResults = result.books.distinctBy { book -> book.id },
                            searchTotal = result.total
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Book search failed. Please retry.", error)
                    _state.update {
                        it.copy(
                            isSearching = false,
                            searchResults = emptyList(),
                            searchTotal = 0,
                            searchError = error.toAppError().toUserMessage()
                        )
                    }
                }
        }
    }

    fun loadMoreSearchResults() {
        val current = _state.value
        if (current.isSearching || current.isLoadingMore || !current.hasMoreSearchResults || current.query.isBlank()) {
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }

            searchBooks(current.query, offset = current.searchResults.size)
                .onSuccess { result ->
                    Bark.i("Loaded more book search results (+${result.books.size}, total: ${result.total})")
                    _state.update {
                        // The backend's underlying Google Books pagination isn't guaranteed
                        // non-overlapping between calls, so a "next page" can repeat a book
                        // already on screen — de-dupe by id, since a repeated grid key crashes
                        // Compose's LazyVerticalGrid outright.
                        val existingIds = it.searchResults.mapTo(mutableSetOf()) { book -> book.id }
                        val newBooks = result.books
                            .filterNot { book -> book.id in existingIds }
                            .distinctBy { book -> book.id }
                        it.copy(
                            isLoadingMore = false,
                            searchResults = it.searchResults + newBooks,
                            searchTotal = result.total
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Failed to load more book search results. Please retry.", error)
                    _state.update { it.copy(isLoadingMore = false) }
                }
        }
    }

    fun onAssignShelf(bookId: String, shelf: ShelfStatus) {
        viewModelScope.launch {
            _state.update { it.copy(isMutationInProgress = true) }

            assignShelf(bookId, shelf)
                .onSuccess { newShelf ->
                    Bark.i("Assigned book (ID: $bookId) to shelf: $newShelf")
                    _state.update { current ->
                        val book = current.shelfEntries.find { it.book.id == bookId }?.book
                            ?: current.searchResults.find { it.id == bookId }
                        val updatedEntries = if (book == null) {
                            current.shelfEntries
                        } else {
                            val idx = current.shelfEntries.indexOfFirst { it.book.id == bookId }
                            val entry = ShelfEntry(shelf = newShelf, book = book)
                            if (idx >= 0) {
                                current.shelfEntries.toMutableList().apply { set(idx, entry) }
                            } else {
                                current.shelfEntries + entry
                            }
                        }
                        current.copy(isMutationInProgress = false, shelfEntries = updatedEntries)
                    }
                    loadShelf(forceRefresh = true)
                }
                .onFailure { error ->
                    Bark.e("Failed to assign shelf (book ID: $bookId). ${error.message}", error)
                    _state.update {
                        it.copy(
                            isMutationInProgress = false,
                            operationError = error.toDisplayMessage()
                        )
                    }
                }
        }
    }

    fun onRemoveFromShelf(bookId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isMutationInProgress = true) }

            removeFromShelf(bookId)
                .onSuccess {
                    Bark.i("Removed book (ID: $bookId) from shelf")
                    _state.update { current ->
                        current.copy(
                            isMutationInProgress = false,
                            shelfEntries = current.shelfEntries.filterNot { it.book.id == bookId }
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Failed to remove book from shelf (book ID: $bookId). ${error.message}", error)
                    _state.update {
                        it.copy(
                            isMutationInProgress = false,
                            operationError = error.toDisplayMessage()
                        )
                    }
                }
        }
    }

    fun onToggleLike(bookId: String) {
        viewModelScope.launch {
            toggleLike(bookId)
                .onSuccess { liked ->
                    Bark.i("Toggled like (book ID: $bookId, liked: $liked)")
                    _state.update { current ->
                        current.copy(
                            likedBookIds = if (liked) {
                                current.likedBookIds + bookId
                            } else {
                                current.likedBookIds - bookId
                            }
                        )
                    }
                }
                .onFailure { error ->
                    Bark.e("Failed to toggle like (book ID: $bookId). Leaving state unchanged.", error)
                }
        }
    }

    fun onConsumeOperationError() {
        _state.update { it.copy(operationError = null) }
    }
}
