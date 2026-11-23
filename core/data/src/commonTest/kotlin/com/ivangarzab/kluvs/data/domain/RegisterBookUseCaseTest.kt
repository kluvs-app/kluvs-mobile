package com.ivangarzab.kluvs.data.domain

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

class RegisterBookUseCaseTest {

    private lateinit var bookRepository: BookRepository
    private lateinit var useCase: RegisterBookUseCase

    private val unregisteredBook = Book(
        id = "",
        title = "The Hobbit",
        author = "J.R.R. Tolkien",
        isbn = "978-0-395-07122-1",
        externalGoogleId = "goog-hobbit"
    )

    @BeforeTest
    fun setup() {
        bookRepository = mock<BookRepository>()
        useCase = RegisterBookUseCase(bookRepository)
    }

    @Test
    fun `invoke returns registered book with real ID`() = runTest {
        val registered = unregisteredBook.copy(id = "42")
        everySuspend { bookRepository.registerBook(any()) } returns Result.success(registered)

        val result = useCase(unregisteredBook)

        assertTrue(result.isSuccess)
        assertEquals("42", result.getOrNull()?.id)
    }

    @Test
    fun `invoke propagates failure from repository`() = runTest {
        val exception = Exception("Network error")
        everySuspend { bookRepository.registerBook(any()) } returns Result.failure(exception)

        val result = useCase(unregisteredBook)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
