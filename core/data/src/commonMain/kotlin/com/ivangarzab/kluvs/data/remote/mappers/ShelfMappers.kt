package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.GetShelf200ResponseDto
import com.ivangarzab.kluvs.api.models.ShelfEntryDto
import com.ivangarzab.kluvs.api.models.ShelfStatusDto
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus

/**
 * Maps a [com.ivangarzab.kluvs.api.models.ShelfStatusDto] from the API to a [ShelfStatus] domain model.
 */
fun ShelfStatusDto.toDomain(): ShelfStatus = when (this) {
    ShelfStatusDto.want_to_read -> ShelfStatus.WANT_TO_READ
    ShelfStatusDto.currently_reading -> ShelfStatus.CURRENTLY_READING
    ShelfStatusDto.read -> ShelfStatus.READ
    ShelfStatusDto.not_finished -> ShelfStatus.NOT_FINISHED
}

/**
 * Maps a [ShelfStatus] domain model to its [ShelfStatusDto] request payload counterpart.
 */
fun ShelfStatus.toDto(): ShelfStatusDto = when (this) {
    ShelfStatus.WANT_TO_READ -> ShelfStatusDto.want_to_read
    ShelfStatus.CURRENTLY_READING -> ShelfStatusDto.currently_reading
    ShelfStatus.READ -> ShelfStatusDto.read
    ShelfStatus.NOT_FINISHED -> ShelfStatusDto.not_finished
}

/**
 * Maps a [com.ivangarzab.kluvs.api.models.ShelfEntryDto] to a [ShelfEntry] domain model,
 * or null when the entry is missing its shelf or book (the domain model requires both).
 */
fun ShelfEntryDto.toDomain(): ShelfEntry? {
    val shelfStatus = shelf ?: return null
    val bookDto = book ?: return null
    return ShelfEntry(
        shelf = shelfStatus.toDomain(),
        source = when (source) {
            ShelfEntryDto.Source.session -> ShelfSource.SESSION
            else -> ShelfSource.MANUAL
        },
        updatedAt = updatedAt.parseDateStringOrNull(),
        book = bookDto.toDomain()
    )
}

/**
 * Maps a shelf list response to domain entries, skipping malformed entries.
 */
fun GetShelf200ResponseDto.toDomain(): List<ShelfEntry> =
    shelves.orEmpty().mapNotNull { it.toDomain() }
