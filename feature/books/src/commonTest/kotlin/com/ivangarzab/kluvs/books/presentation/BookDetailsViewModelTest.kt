package com.ivangarzab.kluvs.books.presentation

import com.ivangarzab.kluvs.books.domain.AssignShelfUseCase
import com.ivangarzab.kluvs.books.domain.GetBookEnrichmentUseCase
import com.ivangarzab.kluvs.books.domain.GetLikeStatusUseCase
import com.ivangarzab.kluvs.data.domain.RegisterBookUseCase
import com.ivangarzab.kluvs.books.domain.RemoveFromShelfUseCase
import com.ivangarzab.kluvs.books.domain.ToggleLikeUseCase
import com.ivangarzab.kluvs.data.repositories.BookEnrichmentRepository
import com.ivangarzab.kluvs.data.repositories.BookRepository
import com.ivangarzab.kluvs.data.repositories.LikeRepository
import com.ivangarzab.kluvs.data.repositories.ShelfRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.AppError
import com.ivangarzab.kluvs.model.BookEnrichment
import com.ivangarzab.kluvs.presentation.effect.ScreenEffect
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.mokkery.verify.VerifyMode.Companion.not
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailsViewModelTest {

    private lateinit var bookEnrichmentRepository: BookEnrichmentRepository
    private lateinit var likeRepository: LikeRepository
    private lateinit var shelfRepository: ShelfRepository
    private lateinit var bookRepository: BookRepository

    private lateinit var getBookEnrichment: GetBookEnrichmentUseCase
    private lateinit var getLikeStatus: GetLikeStatusUseCase
    private lateinit var assignShelf: AssignShelfUseCase
    private lateinit var removeFromShelf: RemoveFromShelfUseCase
    private lateinit var toggleLike: ToggleLikeUseCase
    private lateinit var registerBook: RegisterBookUseCase
    private lateinit var viewModel: BookDetailsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    // A book fresh from search only carries the Google volume ID — never parses as Int.
    private val unregisteredBook = Book(
        id = "zyTCAlFPjgYC",
        title = "The Hobbit",
        author = "J.R.R. Tolkien",
        isbn = "978-0-395-07122-1"
    )
    private val registeredBook = unregisteredBook.copy(id = "42")

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        bookEnrichmentRepository = mock<BookEnrichmentRepository>()
        likeRepository = mock<LikeRepository>()
        shelfRepository = mock<ShelfRepository>()
        bookRepository = mock<BookRepository>()

        getBookEnrichment = GetBookEnrichmentUseCase(bookEnrichmentRepository)
        getLikeStatus = GetLikeStatusUseCase(likeRepository)
        assignShelf = AssignShelfUseCase(shelfRepository)
        removeFromShelf = RemoveFromShelfUseCase(shelfRepository)
        toggleLike = ToggleLikeUseCase(likeRepository)
        registerBook = RegisterBookUseCase(bookRepository)

        everySuspend { bookEnrichmentRepository.getEnrichment(any(), any()) } returns
            Result.success(BookEnrichment(author = null, authorBooks = emptyList(), volumeInfo = null))

        viewModel = BookDetailsViewModel(
            getBookEnrichment, getLikeStatus, assignShelf, removeFromShelf, toggleLike, registerBook
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load registers an unregistered book and exposes the server-assigned id`() = runTest {
        everySuspend { bookRepository.registerBook(unregisteredBook) } returns Result.success(registeredBook)
        everySuspend { likeRepository.isBookLiked("42") } returns Result.success(false)

        viewModel.load(unregisteredBook)

        assertEquals("42", viewModel.state.value.book?.id)
        assertTrue(viewModel.isRegistered)
        verifySuspend { bookRepository.registerBook(unregisteredBook) }
    }

    @Test
    fun `load does not register a book that already has a numeric id`() = runTest {
        everySuspend { likeRepository.isBookLiked("42") } returns Result.success(true)

        viewModel.load(registeredBook)

        assertTrue(viewModel.isRegistered)
        assertTrue(viewModel.state.value.isLiked)
        verifySuspend(mode = not) { bookRepository.registerBook(any()) }
    }

    @Test
    fun `load leaves the book unregistered when registration fails`() = runTest {
        everySuspend { bookRepository.registerBook(unregisteredBook) } returns
            Result.failure(RuntimeException("network error"))

        viewModel.load(unregisteredBook)

        assertFalse(viewModel.isRegistered)
        assertEquals(unregisteredBook.id, viewModel.state.value.book?.id)
        verifySuspend(mode = not) { likeRepository.isBookLiked(any()) }
    }

    @Test
    fun `onToggleLike shows an error snackbar on failure`() = runTest {
        val effects = collectScreenEffects()
        everySuspend { likeRepository.isBookLiked("42") } returns Result.success(false)
        everySuspend { likeRepository.toggleLike("42") } returns Result.failure(AppError.NoConnection)
        viewModel.load(registeredBook)

        viewModel.onToggleLike()

        assertFalse(viewModel.state.value.isLiked)
        assertEquals(ScreenEffect.ShowSnackbar.error("Check your connection and try again."), effects.single())
    }

    /** Collects every screen effect the ViewModel sends from here on. */
    private fun TestScope.collectScreenEffects(): List<ScreenEffect> {
        val effects = mutableListOf<ScreenEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.screenEffects.toList(effects) }
        return effects
    }
}
