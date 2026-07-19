package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.BookDto
import com.ivangarzab.kluvs.model.Book

/**
 * Maps a [com.ivangarzab.kluvs.api.models.BookDto] from the API to a [Book] domain model.
 */
fun BookDto.toDomain(): Book {
    return Book(
        // Google Books search results have no local DB id yet; fall back to the Google volume id
        // so the row still has a stable key. `Book.isRegistered` reflects which case this is.
        id = id?.toString() ?: externalGoogleId.orEmpty(),
        title = title,
        author = author,
        edition = edition,
        year = year,
        isbn = isbn,
        pageCount = pageCount,
        imageUrl = imageUrl,
        externalGoogleId = externalGoogleId
    )
}
