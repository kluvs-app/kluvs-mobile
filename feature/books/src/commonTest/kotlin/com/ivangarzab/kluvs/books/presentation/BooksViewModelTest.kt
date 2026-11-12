package com.ivangarzab.kluvs.books.presentation

import com.ivangarzab.kluvs.books.domain.AssignShelfUseCase
import com.ivangarzab.kluvs.books.domain.GetShelfUseCase
import com.ivangarzab.kluvs.books.domain.RemoveFromShelfUseCase
import com.ivangarzab.kluvs.books.domain.SearchBooksUseCase
import com.ivangarzab.kluvs.books.domain.ToggleLikeUseCase
import com.ivangarzab.kluvs.data.repositories.BookRepository
import com.ivangarzab.kluvs.data.repositories.LikeRepository
import com.ivangarzab.kluvs.data.repositories.ShelfRepository
import com.ivangarzab.kluvs.model.AppError
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.BookSearchResult
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfStatus
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
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
        assertEquals("Something went wrong. Please try again.", state.shelfError)
    }

    // ---- search ----

    @Test
    fun `search updates state with results on success`() = runTest {
        everySuspend { bookRepository.searchBooks(any(), any()) } returns
            Result.success(BookSearchResult(books = listOf(testBook), total = 1))

        viewModel.search("hobbit")

        val state = viewModel.state.value
        assertTrue(!state.isSearching)
        assertEquals(1, state.searchResults.size)
        assertEquals(1, state.searchTotal)
        assertNull(state.searchError)
    }

    @Test
    fun `search updates state with error on failure`() = runTest {
        everySuspend { bookRepository.searchBooks(any(), any()) } returns Result.failure(Exception("Search failed"))

        viewModel.search("hobbit")

        val state = viewModel.state.value
        assertTrue(!state.isSearching)
        assertEquals("Something went wrong. Please try again.", state.searchError)
        assertTrue(state.searchResults.isEmpty())
    }

    @Test
    fun `search with blank query clears results without calling repository`() = runTest {
        viewModel.search("")

        val state = viewModel.state.value
        assertTrue(state.searchResults.isEmpty())
        assertTrue(!state.isSearching)
    }

    @Test
    fun `search replaces previous results and resets paging state on a new query`() = runTest {
        val secondBook = testBook.copy(id = "43", title = "The Fellowship of the Ring")
        everySuspend { bookRepository.searchBooks("hobbit", 0) } returns
            Result.success(BookSearchResult(books = listOf(testBook), total = 2))
        everySuspend { bookRepository.searchBooks("hobbit", 1) } returns
            Result.success(BookSearchResult(books = listOf(secondBook), total = 2))
        viewModel.onQueryChange("hobbit")
        viewModel.search("hobbit")
        viewModel.loadMoreSearchResults()
        assertEquals(2, viewModel.state.value.searchResults.size)

        val otherBook = testBook.copy(id = "99", title = "Dune")
        everySuspend { bookRepository.searchBooks("dune", 0) } returns
            Result.success(BookSearchResult(books = listOf(otherBook), total = 1))
        viewModel.onQueryChange("dune")
        viewModel.search("dune")

        val state = viewModel.state.value
        assertEquals(1, state.searchResults.size)
        assertEquals("Dune", state.searchResults.first().title)
        assertEquals(1, state.searchTotal)
    }

    // ---- loadMoreSearchResults ----

    @Test
    fun `loadMoreSearchResults appends results and advances offset`() = runTest {
        everySuspend { bookRepository.searchBooks("hobbit", 0) } returns
            Result.success(BookSearchResult(books = listOf(testBook), total = 2))
        viewModel.onQueryChange("hobbit")
        viewModel.search("hobbit")

        val secondBook = testBook.copy(id = "43", title = "The Fellowship of the Ring")
        everySuspend { bookRepository.searchBooks("hobbit", 1) } returns
            Result.success(BookSearchResult(books = listOf(secondBook), total = 2))

        viewModel.loadMoreSearchResults()

        val state = viewModel.state.value
        assertTrue(!state.isLoadingMore)
        assertEquals(2, state.searchResults.size)
        assertEquals("The Fellowship of the Ring", state.searchResults.last().title)
        verifySuspend { bookRepository.searchBooks("hobbit", 1) }
    }

    @Test
    fun `loadMoreSearchResults drops books already present from an overlapping next page`() = runTest {
        // Google Books' pagination isn't guaranteed non-overlapping between calls, so the
        // "next page" can repeat a book already in searchResults. A repeated grid key crashes
        // Compose's LazyVerticalGrid outright, so these must never reach the state as dupes.
        everySuspend { bookRepository.searchBooks("hobbit", 0) } returns
            Result.success(BookSearchResult(books = listOf(testBook), total = 3))
        viewModel.onQueryChange("hobbit")
        viewModel.search("hobbit")

        val secondBook = testBook.copy(id = "43", title = "The Fellowship of the Ring")
        everySuspend { bookRepository.searchBooks("hobbit", 1) } returns
            Result.success(BookSearchResult(books = listOf(testBook, secondBook), total = 3))

        viewModel.loadMoreSearchResults()

        val state = viewModel.state.value
        assertEquals(listOf("42", "43"), state.searchResults.map { it.id })
    }

    @Test
    fun `loadMoreSearchResults does nothing once all results are loaded`() = runTest {
        everySuspend { bookRepository.searchBooks("hobbit", 0) } returns
            Result.success(BookSearchResult(books = listOf(testBook), total = 1))
        viewModel.onQueryChange("hobbit")
        viewModel.search("hobbit")

        viewModel.loadMoreSearchResults()

        verifySuspend(mode = dev.mokkery.verify.VerifyMode.exactly(1)) { bookRepository.searchBooks(any(), any()) }
    }

    @Test
    fun `loadMoreSearchResults does nothing without a prior search`() = runTest {
        viewModel.loadMoreSearchResults()

        verifySuspend(mode = dev.mokkery.verify.VerifyMode.not) { bookRepository.searchBooks(any(), any()) }
    }

    // ---- shelf mutations ----

    @Test
    fun `onAssignShelf updates matching shelf entry on success`() = runTest {
        // Refresh after a successful mutation refetches the shelf (with forceRefresh=true), so the
        // mock must match any argument, not just the getShelf() default.
        everySuspend { shelfRepository.getShelf(any()) } returns Result.success(
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
        everySuspend { shelfRepository.assignShelf(any(), any()) } returns Result.failure(AppError.NoConnection)

        viewModel.onAssignShelf("42", ShelfStatus.READ)

        val state = viewModel.state.value
        assertTrue(!state.isMutationInProgress)
        assertEquals("Check your connection and try again.", state.operationError)
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
        everySuspend { shelfRepository.removeFromShelf(any()) } returns Result.failure(AppError.NoConnection)

        viewModel.onRemoveFromShelf("42")

        val state = viewModel.state.value
        assertEquals("Check your connection and try again.", state.operationError)
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
        everySuspend { shelfRepository.assignShelf(any(), any()) } returns Result.failure(AppError.NoConnection)
        viewModel.onAssignShelf("42", ShelfStatus.READ)

        viewModel.onConsumeOperationError()

        assertNull(viewModel.state.value.operationError)
    }
}
