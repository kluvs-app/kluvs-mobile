package com.ivangarzab.kluvs.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity caching one book-enrichment response (volume info, author info, other books by
 * the author), keyed by the request that produced it.
 *
 * Nested objects are stored as JSON strings rather than child tables: the database has no
 * TypeConverters, the data is read/written as a whole, and author data is small enough that
 * duplicating it across a few books by the same author is fine. The JSON encoding lives in
 * `:core:data`'s mappers, not here.
 *
 * @property cacheKey Request key: Google Books volume id plus primary author name
 * @property volumeInfoJson Encoded volume info, or null when the response had none
 * @property authorJson Encoded author info, or null when the response had none
 * @property authorBooksJson Encoded list of other books by the author
 */
@Entity(tableName = "book_enrichment")
data class BookEnrichmentEntity(
    @PrimaryKey val cacheKey: String,
    val volumeInfoJson: String?,
    val authorJson: String?,
    val authorBooksJson: String,
    val lastFetchedAt: Long // Timestamp in milliseconds for TTL check
)
