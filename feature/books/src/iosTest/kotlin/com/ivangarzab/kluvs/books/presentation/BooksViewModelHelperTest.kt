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
import dev.mokkery.mock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BooksViewModelHelperTest {

    private lateinit var shelfRepository: ShelfRepository
    private lateinit var likeRepository: LikeRepository
    private lateinit var bookRepository: BookRepository
    private lateinit var viewModel: BooksViewModel
    private lateinit var testScope: CoroutineScope
    private lateinit var helper: BooksViewModelHelper
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        shelfRepository = mock<ShelfRepository>()
        likeRepository = mock<LikeRepository>()
        bookRepository = mock<BookRepository>()

        testScope = CoroutineScope(testDispatcher + Job())

        viewModel = BooksViewModel(
            getShelf = GetShelfUseCase(shelfRepository),
            assignShelf = AssignShelfUseCase(shelfRepository),
            removeFromShelf = RemoveFromShelfUseCase(shelfRepository),
            toggleLike = ToggleLikeUseCase(likeRepository),
            searchBooks = SearchBooksUseCase(bookRepository)
        )

        startKoin {
            modules(
                module {
                    single<BooksViewModel> { viewModel }
                    single<CoroutineScope> { testScope }
                }
            )
        }

        helper = BooksViewModelHelper()
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        testScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `observeState immediately calls callback with current state`() = runTest {
        var callbackInvoked = false
        var receivedState: BooksState? = null

        val closeable = helper.observeState { state ->
            callbackInvoked = true
            receivedState = state
        }

        assertTrue(callbackInvoked, "Callback should be invoked immediately")
        assertNotNull(receivedState)
        assertTrue(receivedState!!.isLoadingShelf)

        closeable.close()
    }

    @Test
    fun `observeState receives updated states when loadShelf is called`() = runTest {
        val receivedStates = mutableListOf<BooksState>()
        val closeable = helper.observeState { state ->
            receivedStates.add(state)
        }

        val entries = listOf(
            ShelfEntry(shelf = ShelfStatus.READ, book = Book(id = "1", title = "Dune", author = "Frank Herbert", isbn = null))
        )
        everySuspend { shelfRepository.getShelf() } returns Result.success(entries)

        helper.loadShelf(forceRefresh = false)

        assertTrue(receivedStates.size >= 2, "Should receive initial state + at least one update")
        assertEquals(entries, receivedStates.last().shelfEntries)

        closeable.close()
    }

    @Test
    fun `closeable stops receiving updates when closed`() = runTest {
        val receivedStates = mutableListOf<BooksState>()
        val closeable = helper.observeState { state ->
            receivedStates.add(state)
        }

        val sizeBefore = receivedStates.size
        closeable.close()

        everySuspend { shelfRepository.getShelf() } returns Result.success(emptyList())
        helper.loadShelf(forceRefresh = false)

        assertEquals(sizeBefore, receivedStates.size, "Should not receive state emitted after closing")
    }

    @Test
    fun `search forwards query to ViewModel`() = runTest {
        val books = listOf(Book(id = "42", title = "Project Hail Mary", author = "Andy Weir", isbn = null))
        everySuspend { bookRepository.searchBooks("hail") } returns Result.success(books)

        val receivedStates = mutableListOf<BooksState>()
        val closeable = helper.observeState { state -> receivedStates.add(state) }

        helper.search("hail")

        assertEquals(books, receivedStates.last().searchResults)
        closeable.close()
    }

    @Test
    fun `onQueryChange updates query in state`() = runTest {
        val receivedStates = mutableListOf<BooksState>()
        val closeable = helper.observeState { state -> receivedStates.add(state) }

        helper.onQueryChange("dune")

        assertEquals("dune", receivedStates.last().query)
        closeable.close()
    }
}
