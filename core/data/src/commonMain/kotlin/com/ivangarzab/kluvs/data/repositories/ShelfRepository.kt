package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ShelfAssignRequestDto
import com.ivangarzab.kluvs.data.local.cache.CachePolicy
import com.ivangarzab.kluvs.data.local.cache.CacheTTL
import com.ivangarzab.kluvs.data.local.source.ShelfLocalDataSource
import com.ivangarzab.kluvs.data.remote.mappers.toDto
import com.ivangarzab.kluvs.data.remote.source.ShelfRemoteDataSource
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfStatus

/**
 * Repository for the authenticated member's book shelf.
 *
 * All operations are member-scoped: the backend resolves the member from the
 * caller's auth token, so a signed-in user session is required.
 */
interface ShelfRepository {

    /**
     * Retrieves the member's full shelf.
     *
     * @return Result containing every [ShelfEntry] (with book populated) if successful,
     *         or an error if the operation failed
     */
    suspend fun getShelf(): Result<List<ShelfEntry>>

    /**
     * Retrieves the member's shelf status for a single book.
     *
     * @param bookId The ID of the book to look up
     * @return Result containing the [ShelfStatus], or null if the book is not shelved
     */
    suspend fun getShelfStatus(bookId: String): Result<ShelfStatus?>

    /**
     * Assigns a book to a shelf, moving it from any shelf it was on before.
     *
     * @param bookId The ID of the book to shelve
     * @param shelf The shelf to assign the book to
     * @return Result containing the [ShelfStatus] the book landed on if successful
     */
    suspend fun assignShelf(bookId: String, shelf: ShelfStatus): Result<ShelfStatus>

    /**
     * Removes a book from the member's shelf entirely.
     *
     * @param bookId The ID of the book to remove
     * @return Result indicating success or failure
     */
    suspend fun removeFromShelf(bookId: String): Result<Unit>
}

/**
 * Implementation of [ShelfRepository] with TTL-based caching.
 *
 * Implements a cache-aside pattern for reads. Mutations (assign/remove) can't
 * write a full [ShelfEntry] back to the cache since the backend only returns
 * the resulting [ShelfStatus] (not source/updatedAt/book), so they instead
 * invalidate the cached entry, forcing the next read to refresh from remote.
 */
internal class ShelfRepositoryImpl(
    private val shelfRemoteDataSource: ShelfRemoteDataSource,
    private val shelfLocalDataSource: ShelfLocalDataSource,
    private val cachePolicy: CachePolicy
) : ShelfRepository {

    override suspend fun getShelf(): Result<List<ShelfEntry>> {
        val cachedShelf = shelfLocalDataSource.getShelf()
        if (cachedShelf.isNotEmpty()) {
            val lastFetchedAt = shelfLocalDataSource.getLastFetchedAt(cachedShelf.first().book.id)
            if (!cachePolicy.isStale(lastFetchedAt, CacheTTL.SHELF)) {
                Bark.d("Cache hit for shelf")
                return Result.success(cachedShelf)
            }
        }
        Bark.d("Cache miss for shelf")

        val result = shelfRemoteDataSource.getShelf()

        result.onSuccess { entries ->
            Bark.v("Persisting shelf to cache (${entries.size} entries)")
            try {
                shelfLocalDataSource.insertShelfEntries(entries)
                Bark.d("Shelf cached (${entries.size} entries)")
            } catch (e: Exception) {
                Bark.e("Shelf cache failed. Will use remote data on next fetch.", e)
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch shelf. Cached data may be unavailable.", error)
        }

        return result
    }

    override suspend fun getShelfStatus(bookId: String): Result<ShelfStatus?> {
        val bookIdInt = bookId.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid book ID: $bookId"))

        val cachedEntry = shelfLocalDataSource.getShelfEntry(bookId)
        val lastFetchedAt = shelfLocalDataSource.getLastFetchedAt(bookId)

        if (cachedEntry != null && !cachePolicy.isStale(lastFetchedAt, CacheTTL.SHELF)) {
            Bark.d("Cache hit for shelf status (book ID: $bookId)")
            return Result.success(cachedEntry.shelf)
        }
        Bark.d("Cache miss for shelf status (book ID: $bookId)")

        val result = shelfRemoteDataSource.getShelfStatus(bookIdInt)

        result.onFailure { error ->
            Bark.e("Failed to fetch shelf status. Cached data may be unavailable.", error)
        }

        return result
    }

    override suspend fun assignShelf(bookId: String, shelf: ShelfStatus): Result<ShelfStatus> {
        val bookIdInt = bookId.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid book ID: $bookId"))
        Bark.d("Assigning book (ID: $bookId) to shelf: $shelf")
        val result = shelfRemoteDataSource.assignShelf(
            ShelfAssignRequestDto(
                bookId = bookIdInt,
                shelf = shelf.toDto()
            )
        )

        result.onSuccess {
            Bark.v("Invalidating shelf cache (book ID: $bookId) after assignment")
            shelfLocalDataSource.deleteShelfEntry(bookId)
        }

        return result
    }

    override suspend fun removeFromShelf(bookId: String): Result<Unit> {
        val bookIdInt = bookId.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid book ID: $bookId"))
        Bark.d("Removing book (ID: $bookId) from shelf")
        val result = shelfRemoteDataSource.removeShelf(bookIdInt)

        result.onSuccess {
            Bark.v("Removing shelf entry from cache (book ID: $bookId)")
            shelfLocalDataSource.deleteShelfEntry(bookId)
        }

        return result
    }
}
