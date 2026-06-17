package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.BookDto
import com.ivangarzab.kluvs.model.Book

/**
 * Maps a generated [com.ivangarzab.kluvs.api.models.Book] wire DTO to a [Book] domain model.
 */
fun BookDto.toDomain(): Book {
    return Book(
        id = id.toString(),
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
