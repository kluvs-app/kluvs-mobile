package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.BookRegistrationRequestDto
import com.ivangarzab.kluvs.data.remote.api.BookService
import com.ivangarzab.kluvs.data.remote.mappers.toDomain
import com.ivangarzab.kluvs.model.Book

/**
 * Remote data source for Book operations.
 *
 * Responsibilities:
 * - Calls [BookService] to fetch/register book data from the API
 * - Maps DTOs to domain models using mappers
 * - Wraps results in [Result] for error handling
 */
interface BookRemoteDataSource {

    /**
     * Searches for books matching the given query.
     *
     * @param query Free-text search query
     * @return Result containing a list of matching [Book]s, or an error
     */
    suspend fun searchBooks(query: String): Result<List<Book>>

    /**
     * Registers a book with the API (creates if not exists, or returns existing).
     *
     * @param book The book to register
     * @return Result containing the registered [Book] (with a server-assigned id), or an error
     */
    suspend fun registerBook(book: Book): Result<Book>
}

class BookRemoteDataSourceImpl(
    private val bookService: BookService
) : BookRemoteDataSource {

    override suspend fun searchBooks(query: String): Result<List<Book>> {
        return try {
            val response = bookService.search(query)
            val books = response.books ?: emptyList()
            Bark.i("Book search returned ${books.size} results (query: \"$query\")")
            Result.success(books.map { it.toDomain() })
        } catch (e: Exception) {
            Bark.e("Failed to search books (query: \"$query\").", e)
            Result.failure(e)
        }
    }

    override suspend fun registerBook(book: Book): Result<Book> {
        return try {
            val response = bookService.register(
                BookRegistrationRequestDto(
                    title = book.title,
                    author = book.author,
                    year = book.year,
                    isbn = book.isbn,
                    pageCount = book.pageCount,
                    imageUrl = book.imageUrl,
                    externalGoogleId = book.externalGoogleId
                )
            )
            val registeredBook = response.book ?: error("Book registration response missing book data")
            Bark.i("Book registered (created=${response.created}): ${book.title}")
            Result.success(registeredBook.toDomain())
        } catch (e: Exception) {
            Bark.e("Failed to register book (${book.title}).", e)
            Result.failure(e)
        }
    }
}
