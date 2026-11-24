package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.kluvs.api.models.ShelfEntryDto
import com.ivangarzab.kluvs.api.models.ShelfListResponseDto
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
 * Maps a [com.ivangarzab.kluvs.api.models.ShelfEntryDto] to a [ShelfEntry] domain model.
 */
fun ShelfEntryDto.toDomain(): ShelfEntry {
    return ShelfEntry(
        shelf = shelf.toDomain(),
        source = when (source) {
            ShelfEntryDto.Source.session -> ShelfSource.SESSION
            ShelfEntryDto.Source.manual -> ShelfSource.MANUAL
        },
        updatedAt = updatedAt.parseDateStringOrNull(),
        book = book.toDomain()
    )
}

/**
 * Maps a shelf list response to domain entries.
 */
fun ShelfListResponseDto.toDomain(): List<ShelfEntry> =
    shelves.map { it.toDomain() }
