package com.ivangarzab.kluvs.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity recording when a progress query was last fetched from the backend.
 *
 * Progress rows carry their own `lastFetchedAt`, but a query that matched no rows leaves nothing
 * to timestamp, so it could never count as fresh and was refetched on every read. This row is
 * that missing timestamp, and it covers empty results too.
 *
 * @property queryKey The query's filters (book, session, status), encoded by `:core:data`
 */
@Entity(tableName = "progress_query")
data class ProgressQueryEntity(
    @PrimaryKey val queryKey: String,
    val lastFetchedAt: Long // Timestamp in milliseconds for TTL check
)
