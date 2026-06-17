package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.data.remote.dtos.BookDto
import com.ivangarzab.kluvs.model.Book

/**
 * Maps a [com.ivangarzab.kluvs.data.remote.dtos.BookDto] from the API to a [Book] domain model.
 */
fun BookDto.toDomain(): Book {
    return Book(
        id = id,
        title = title,
        author = author,
        edition = edition,
        year = year,
        isbn = isbn,
        pageCount = page_count,
        imageUrl = image_url,
        externalGoogleId = external_google_id
    )
}
