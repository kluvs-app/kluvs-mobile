package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.AuthorBookDto
import com.ivangarzab.kluvs.api.models.AuthorInfoDto
import com.ivangarzab.kluvs.api.models.BookEnrichmentResponseDto
import com.ivangarzab.kluvs.api.models.BookIndustryIdentifierDto
import com.ivangarzab.kluvs.api.models.BookVolumeInfoDto
import com.ivangarzab.kluvs.model.Author
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.BookEnrichment
import com.ivangarzab.kluvs.model.BookVolumeInfo

/**
 * Maps a [BookEnrichmentResponseDto] to a [BookEnrichment] domain model.
 *
 * @param excludeExternalGoogleId when non-null, filters that book out of [authorBooks] —
 * a defensive client-side filter mirroring the backend's own exclusion of the requested book.
 */
fun BookEnrichmentResponseDto.toDomain(excludeExternalGoogleId: String? = null): BookEnrichment {
    return BookEnrichment(
        volumeInfo = volumeInfo?.toDomain(),
        author = authorInfo?.toDomain(),
        authorBooks = authorBooks
            .filter { it.externalGoogleId != excludeExternalGoogleId }
            .map { it.toDomain() }
    )
}

fun BookVolumeInfoDto.toDomain(): BookVolumeInfo {
    return BookVolumeInfo(
        subtitle = subtitle,
        publisher = publisher,
        description = description,
        categories = categories,
        language = language,
        isbn13 = industryIdentifiers.firstOrNull { it.type == BookIndustryIdentifierDto.Type.ISBN_13 }?.identifier,
        isbn10 = industryIdentifiers.firstOrNull { it.type == BookIndustryIdentifierDto.Type.ISBN_10 }?.identifier
    )
}

fun AuthorInfoDto.toDomain(): Author {
    return Author(
        name = name,
        imageUrl = imageUrl,
        bio = extract
    )
}

fun AuthorBookDto.toDomain(): Book {
    return Book(
        id = externalGoogleId,
        title = title,
        author = authors.joinToString(", "),
        isbn = null,
        imageUrl = imageUrl,
        year = publishedDate?.take(4)?.toIntOrNull(),
        externalGoogleId = externalGoogleId
    )
}
