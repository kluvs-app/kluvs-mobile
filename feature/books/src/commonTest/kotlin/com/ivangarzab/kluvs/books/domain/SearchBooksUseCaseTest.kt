package com.ivangarzab.kluvs.books.domain

import com.ivangarzab.kluvs.data.repositories.BookRepository
import com.ivangarzab.kluvs.model.Book
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
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
        everySuspend { bookRepository.searchBooks(any()) } returns Result.success(listOf(testBook))

        val result = useCase("hobbit")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("The Hobbit", result.getOrNull()?.first()?.title)
    }

    @Test
    fun `invoke returns empty list when no results`() = runTest {
        everySuspend { bookRepository.searchBooks(any()) } returns Result.success(emptyList())

        val result = useCase("xyzzy")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val exception = Exception("Network error")
        everySuspend { bookRepository.searchBooks(any()) } returns Result.failure(exception)

        val result = useCase("hobbit")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
