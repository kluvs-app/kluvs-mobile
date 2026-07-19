package com.ivangarzab.kluvs.books.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.books.domain.AssignShelfUseCase
import com.ivangarzab.kluvs.books.domain.GetShelfUseCase
import com.ivangarzab.kluvs.books.domain.RemoveFromShelfUseCase
import com.ivangarzab.kluvs.books.domain.SearchBooksUseCase
import com.ivangarzab.kluvs.books.domain.ToggleLikeUseCase
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

            getShelf()
                .onSuccess { entries ->
                    Bark.i("Loaded shelf (${entries.size} entries)")
                    _state.update { it.copy(isLoadingShelf = false, shelfEntries = entries) }
                }
                .onFailure { error ->
                    Bark.e("Failed to load shelf. Please retry.", error)
                    _state.update {
                        it.copy(
                            isLoadingShelf = false,
                            shelfError = error.message ?: "Failed to load shelf"
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
            _state.update { it.copy(searchResults = emptyList(), searchError = null, isSearching = false) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, searchError = null) }

            searchBooks(query)
                .onSuccess { books ->
                    Bark.i("Book search complete (${books.size} results)")
                    _state.update { it.copy(isSearching = false, searchResults = books) }
                }
                .onFailure { error ->
                    Bark.e("Book search failed. Please retry.", error)
                    _state.update {
                        it.copy(
                            isSearching = false,
                            searchResults = emptyList(),
                            searchError = error.message ?: "Search failed"
                        )
                    }
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
                            operationError = error.message ?: "Failed to update shelf"
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
                            operationError = error.message ?: "Failed to update shelf"
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
