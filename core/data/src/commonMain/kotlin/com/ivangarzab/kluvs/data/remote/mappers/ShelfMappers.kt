package com.ivangarzab.kluvs.data.remote.mappers

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ShelfEntryDto
import com.ivangarzab.kluvs.api.models.ShelfListResponseDto
import com.ivangarzab.kluvs.api.models.ShelfStatusDto
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus

/**
 * Maps a [com.ivangarzab.kluvs.api.models.ShelfStatusDto] from the API to a [ShelfStatus] domain model.
 *
 * Returns null for a status this app version doesn't know: no shelf is a safe stand-in
 * for any of them, so callers drop the entry or use a value they already know instead.
 */
fun ShelfStatusDto.toDomain(): ShelfStatus? = when (this) {
    ShelfStatusDto.want_to_read -> ShelfStatus.WANT_TO_READ
    ShelfStatusDto.currently_reading -> ShelfStatus.CURRENTLY_READING
    ShelfStatusDto.read -> ShelfStatus.READ
    ShelfStatusDto.not_finished -> ShelfStatus.NOT_FINISHED
    ShelfStatusDto.unknown_default_open_api -> {
        Bark.w("Unknown shelf status from API; ignoring it")
        null
    }
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
 *
 * Returns null (the entry is dropped) when its shelf status is unknown to this app version.
 */
fun ShelfEntryDto.toDomain(): ShelfEntry? {
    val shelf = shelf.toDomain() ?: return null
    return ShelfEntry(
        shelf = shelf,
        source = when (source) {
            ShelfEntryDto.Source.session -> ShelfSource.SESSION
            ShelfEntryDto.Source.manual -> ShelfSource.MANUAL
            // MANUAL makes no claim; SESSION would show the "read" ribbon.
            ShelfEntryDto.Source.unknown_default_open_api -> {
                Bark.w("Unknown shelf source from API; falling back to MANUAL (Book ID: ${book.id})")
                ShelfSource.MANUAL
            }
        },
        updatedAt = updatedAt.parseDateStringOrNull(),
        book = book.toDomain()
    )
}

/**
 * Maps a shelf list response to domain entries.
 */
fun ShelfListResponseDto.toDomain(): List<ShelfEntry> =
    shelves.mapNotNull { it.toDomain() }
