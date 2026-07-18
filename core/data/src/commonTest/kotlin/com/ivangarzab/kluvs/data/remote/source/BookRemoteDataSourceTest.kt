package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.kluvs.api.models.BookDto
import com.ivangarzab.kluvs.data.remote.api.BookService
import com.ivangarzab.kluvs.api.models.BookRegistrationResponseDto
import com.ivangarzab.kluvs.api.models.BookSearchResponseDto
import com.ivangarzab.kluvs.model.Book
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BookRemoteDataSourceTest {

    private lateinit var bookService: BookService
    private lateinit var dataSource: BookRemoteDataSource

    private val testBookDto = BookDto(
        id = 42,
        title = "The Hobbit",
        author = "J.R.R. Tolkien",
        year = 1937,
        isbn = "978-0-395-07122-1",
        pageCount = 310,
        imageUrl = "https://example.com/hobbit.jpg",
        externalGoogleId = "goog-hobbit"
    )

    private val testBook = Book(
        id = "42",
        title = "The Hobbit",
        author = "J.R.R. Tolkien",
        year = 1937,
        isbn = "978-0-395-07122-1",
        pageCount = 310,
        imageUrl = "https://example.com/hobbit.jpg",
        externalGoogleId = "goog-hobbit"
    )

    @BeforeTest
    fun setup() {
        bookService = mock<BookService>()
        dataSource = BookRemoteDataSourceImpl(bookService)
    }

    // ========================================
    // SEARCH BOOKS
    // ========================================

    @Test
    fun `searchBooks success returns list of books`() = runTest {
        everySuspend { bookService.search(any(), any()) } returns BookSearchResponseDto(
            success = true,
            books = listOf(testBookDto)
        )

        val result = dataSource.searchBooks("hobbit")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("The Hobbit", result.getOrNull()?.first()?.title)
        assertEquals("goog-hobbit", result.getOrNull()?.first()?.externalGoogleId)
    }

    @Test
    fun `searchBooks returns empty list when no results`() = runTest {
        everySuspend { bookService.search(any(), any()) } returns BookSearchResponseDto(
            success = true,
            books = emptyList()
        )

        val result = dataSource.searchBooks("xyzzy")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `searchBooks failure returns Result failure`() = runTest {
        val exception = Exception("Network error")
        everySuspend { bookService.search(any(), any()) } throws exception

        val result = dataSource.searchBooks("hobbit")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    // ========================================
    // REGISTER BOOK
    // ========================================

    @Test
    fun `registerBook success returns registered book`() = runTest {
        val response = BookRegistrationResponseDto(
            success = true,
            book = testBookDto,
            created = true
        )
        everySuspend { bookService.register(any()) } returns response

        val result = dataSource.registerBook(testBook)

        assertTrue(result.isSuccess)
        assertEquals("42", result.getOrNull()?.id)
        assertEquals("The Hobbit", result.getOrNull()?.title)
        assertEquals("goog-hobbit", result.getOrNull()?.externalGoogleId)
    }

    @Test
    fun `registerBook returns existing book when already registered`() = runTest {
        val response = BookRegistrationResponseDto(
            success = true,
            book = testBookDto,
            created = false,
            message = "Book already exists"
        )
        everySuspend { bookService.register(any()) } returns response

        val result = dataSource.registerBook(testBook)

        assertTrue(result.isSuccess)
        assertEquals("42", result.getOrNull()?.id)
    }

    @Test
    fun `registerBook failure returns Result failure`() = runTest {
        val exception = Exception("Server error")
        everySuspend { bookService.register(any()) } throws exception

        val result = dataSource.registerBook(testBook)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
