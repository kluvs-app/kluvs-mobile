package com.ivangarzab.kluvs.books.presentation

import com.ivangarzab.kluvs.books.domain.AssignShelfUseCase
import com.ivangarzab.kluvs.books.domain.GetShelfUseCase
import com.ivangarzab.kluvs.books.domain.RemoveFromShelfUseCase
import com.ivangarzab.kluvs.books.domain.SearchBooksUseCase
import com.ivangarzab.kluvs.books.domain.ToggleLikeUseCase
import com.ivangarzab.kluvs.data.repositories.BookRepository
import com.ivangarzab.kluvs.data.repositories.LikeRepository
import com.ivangarzab.kluvs.data.repositories.ShelfRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfStatus
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BooksViewModelTest {

    private lateinit var shelfRepository: ShelfRepository
    private lateinit var likeRepository: LikeRepository
    private lateinit var bookRepository: BookRepository
    private lateinit var getShelf: GetShelfUseCase
    private lateinit var assignShelf: AssignShelfUseCase
    private lateinit var removeFromShelf: RemoveFromShelfUseCase
    private lateinit var toggleLike: ToggleLikeUseCase
    private lateinit var searchBooks: SearchBooksUseCase
    private lateinit var viewModel: BooksViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testBook = Book(id = "42", title = "The Hobbit", author = "J.R.R. Tolkien", isbn = "978-0-395-07122-1")
    private val testEntry = ShelfEntry(shelf = ShelfStatus.CURRENTLY_READING, book = testBook)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        shelfRepository = mock<ShelfRepository>()
        likeRepository = mock<LikeRepository>()
        bookRepository = mock<BookRepository>()

        // Use REAL UseCases with mocked repositories
        getShelf = GetShelfUseCase(shelfRepository)
        assignShelf = AssignShelfUseCase(shelfRepository)
        removeFromShelf = RemoveFromShelfUseCase(shelfRepository)
        toggleLike = ToggleLikeUseCase(likeRepository)
        searchBooks = SearchBooksUseCase(bookRepository)

        viewModel = BooksViewModel(getShelf, assignShelf, removeFromShelf, toggleLike, searchBooks)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---- loadShelf ----

    @Test
    fun `loadShelf updates state with entries on success`() = runTest {
        everySuspend { shelfRepository.getShelf() } returns Result.success(listOf(testEntry))

        viewModel.loadShelf()

        val state = viewModel.state.value
        assertTrue(!state.isLoadingShelf)
        assertEquals(1, state.shelfEntries.size)
        assertNull(state.shelfError)
    }

    @Test
    fun `loadShelf updates state with error on failure`() = runTest {
        everySuspend { shelfRepository.getShelf() } returns Result.failure(Exception("Network error"))

        viewModel.loadShelf()

        val state = viewModel.state.value
        assertTrue(!state.isLoadingShelf)
        assertEquals("Network error", state.shelfError)
    }

    // ---- search ----

    @Test
    fun `search updates state with results on success`() = runTest {
        everySuspend { bookRepository.searchBooks(any()) } returns Result.success(listOf(testBook))

        viewModel.search("hobbit")

        val state = viewModel.state.value
        assertTrue(!state.isSearching)
        assertEquals(1, state.searchResults.size)
        assertNull(state.searchError)
    }

    @Test
    fun `search updates state with error on failure`() = runTest {
        everySuspend { bookRepository.searchBooks(any()) } returns Result.failure(Exception("Search failed"))

        viewModel.search("hobbit")

        val state = viewModel.state.value
        assertTrue(!state.isSearching)
        assertEquals("Search failed", state.searchError)
        assertTrue(state.searchResults.isEmpty())
    }

    @Test
    fun `search with blank query clears results without calling repository`() = runTest {
        viewModel.search("")

        val state = viewModel.state.value
        assertTrue(state.searchResults.isEmpty())
        assertTrue(!state.isSearching)
    }

    // ---- shelf mutations ----

    @Test
    fun `onAssignShelf updates matching shelf entry on success`() = runTest {
        // Refresh after a successful mutation refetches the shelf, so the mock reflects the new state
        everySuspend { shelfRepository.getShelf() } returns Result.success(
            listOf(testEntry.copy(shelf = ShelfStatus.READ))
        )
        everySuspend { shelfRepository.assignShelf(any(), any()) } returns Result.success(ShelfStatus.READ)

        viewModel.onAssignShelf("42", ShelfStatus.READ)

        val state = viewModel.state.value
        assertTrue(!state.isMutationInProgress)
        assertEquals(ShelfStatus.READ, state.shelfEntries.first { it.book.id == "42" }.shelf)
    }

    @Test
    fun `onAssignShelf sets operationError on failure`() = runTest {
        everySuspend { shelfRepository.assignShelf(any(), any()) } returns Result.failure(Exception("Update failed"))

        viewModel.onAssignShelf("42", ShelfStatus.READ)

        val state = viewModel.state.value
        assertTrue(!state.isMutationInProgress)
        assertEquals("Update failed", state.operationError)
    }

    @Test
    fun `onRemoveFromShelf removes entry from state on success`() = runTest {
        everySuspend { shelfRepository.getShelf() } returns Result.success(listOf(testEntry))
        viewModel.loadShelf()

        everySuspend { shelfRepository.removeFromShelf(any()) } returns Result.success(Unit)
        viewModel.onRemoveFromShelf("42")

        val state = viewModel.state.value
        assertTrue(state.shelfEntries.none { it.book.id == "42" })
    }

    @Test
    fun `onRemoveFromShelf sets operationError on failure`() = runTest {
        everySuspend { shelfRepository.removeFromShelf(any()) } returns Result.failure(Exception("Remove failed"))

        viewModel.onRemoveFromShelf("42")

        val state = viewModel.state.value
        assertEquals("Remove failed", state.operationError)
    }

    // ---- like ----

    @Test
    fun `onToggleLike adds book to likedBookIds when liked`() = runTest {
        everySuspend { likeRepository.toggleLike(any()) } returns Result.success(true)

        viewModel.onToggleLike("42")

        assertTrue(viewModel.state.value.likedBookIds.contains("42"))
    }

    @Test
    fun `onToggleLike removes book from likedBookIds when unliked`() = runTest {
        everySuspend { likeRepository.toggleLike(any()) } returns Result.success(false)

        viewModel.onToggleLike("42")

        assertTrue(!viewModel.state.value.likedBookIds.contains("42"))
    }

    @Test
    fun `onToggleLike leaves state unchanged on failure`() = runTest {
        everySuspend { likeRepository.toggleLike(any()) } returns Result.failure(Exception("Toggle failed"))

        viewModel.onToggleLike("42")

        assertTrue(viewModel.state.value.likedBookIds.isEmpty())
    }

    // ---- UI event consumption ----

    @Test
    fun `onConsumeOperationError clears operationError`() = runTest {
        everySuspend { shelfRepository.assignShelf(any(), any()) } returns Result.failure(Exception("Update failed"))
        viewModel.onAssignShelf("42", ShelfStatus.READ)

        viewModel.onConsumeOperationError()

        assertNull(viewModel.state.value.operationError)
    }
}
