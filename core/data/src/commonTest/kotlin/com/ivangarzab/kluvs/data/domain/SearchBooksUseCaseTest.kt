package com.ivangarzab.kluvs.data.domain

import com.ivangarzab.kluvs.data.repositories.BookRepository
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.BookSearchResult
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchBooksUseCaseTest {

    private lateinit var bookRepository: BookRepository
    private lateinit var useCase: SearchBooksUseCase

    private val testBook = Book(
        id = "42",
        title = "The Hobbit",
        author = "J.R.R. Tolkien",
        isbn = "978-0-395-07122-1",
        externalGoogleId = "goog-hobbit"
    )

    @BeforeTest
    fun setup() {
        bookRepository = mock<BookRepository>()
        useCase = SearchBooksUseCase(bookRepository)
    }

    @Test
    fun `invoke returns books from repository`() = runTest {
        everySuspend { bookRepository.searchBooks(any(), any()) } returns
            Result.success(BookSearchResult(books = listOf(testBook), total = 1))

        val result = useCase("hobbit")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.books?.size)
        assertEquals("The Hobbit", result.getOrNull()?.books?.first()?.title)
        assertEquals(1, result.getOrNull()?.total)
    }

    @Test
    fun `invoke returns empty list when no results`() = runTest {
        everySuspend { bookRepository.searchBooks(any(), any()) } returns
            Result.success(BookSearchResult(books = emptyList(), total = 0))

        val result = useCase("xyzzy")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.books?.size)
    }

    @Test
    fun `invoke passes offset through to the repository`() = runTest {
        everySuspend { bookRepository.searchBooks(any(), any()) } returns
            Result.success(BookSearchResult(books = listOf(testBook), total = 25))

        useCase("hobbit", offset = 10)

        verifySuspend { bookRepository.searchBooks("hobbit", 10) }
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val exception = Exception("Network error")
        everySuspend { bookRepository.searchBooks(any(), any()) } returns Result.failure(exception)

        val result = useCase("hobbit")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
