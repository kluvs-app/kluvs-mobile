package com.ivangarzab.kluvs.data.local.mappers

import com.ivangarzab.kluvs.data.remote.mappers.parseDateStringOrNull
import com.ivangarzab.kluvs.database.entities.ShelfEntity
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfSource
import com.ivangarzab.kluvs.model.ShelfStatus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Maps a [ShelfEntity] from the local database to a [ShelfEntry] domain model.
 * Requires the associated [Book] to be loaded separately.
 */
fun ShelfEntity.toDomain(book: Book): ShelfEntry {
    return ShelfEntry(
        shelf = ShelfStatus.valueOf(shelf),
        source = ShelfSource.valueOf(source),
        updatedAt = updatedAt.parseDateStringOrNull(),
        book = book
    )
}

/**
 * Maps a [ShelfEntry] domain model to a [ShelfEntity] for local database storage.
 * Sets lastFetchedAt to current time.
 */
@OptIn(ExperimentalTime::class)
fun ShelfEntry.toEntity(): ShelfEntity {
    return ShelfEntity(
        bookId = book.id,
        shelf = shelf.name,
        source = source.name,
        updatedAt = updatedAt?.toString(),
        lastFetchedAt = Clock.System.now().toEpochMilliseconds()
    )
}
