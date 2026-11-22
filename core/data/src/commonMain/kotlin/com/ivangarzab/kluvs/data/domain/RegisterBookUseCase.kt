package com.ivangarzab.kluvs.data.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.BookRepository
import com.ivangarzab.kluvs.model.Book

/**
 * UseCase for registering a book with the backend, obtaining a server-assigned ID.
 *
 * Books arriving fresh from search (or a "more by this author" row) only carry a Google
 * Books volume ID until registered. They need a real, persisted [Book.id] before they can
 * be shelved, liked, or used as a session's `book_id`. The backend creates the book if it
 * doesn't already exist, or returns the existing record otherwise.
 *
 * Shared by `:feature:books` and `:feature:clubs`; a feature that needs extra logic wraps
 * this in its own UseCase rather than copying it.
 *
 * @param bookRepository Repository for book data
 */
class RegisterBookUseCase(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(book: Book): Result<Book> {
        Bark.d("Registering book via use case (title: \"${book.title}\")")
        return bookRepository.registerBook(book)
    }
}
