package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ShelfAssignRequestDto
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

internal class ShelfRepositoryImpl(
    private val shelfRemoteDataSource: ShelfRemoteDataSource
) : ShelfRepository {

    override suspend fun getShelf(): Result<List<ShelfEntry>> {
        return shelfRemoteDataSource.getShelf()
    }

    override suspend fun getShelfStatus(bookId: String): Result<ShelfStatus?> {
        val bookIdInt = bookId.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid book ID: $bookId"))
        return shelfRemoteDataSource.getShelfStatus(bookIdInt)
    }

    override suspend fun assignShelf(bookId: String, shelf: ShelfStatus): Result<ShelfStatus> {
        val bookIdInt = bookId.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid book ID: $bookId"))
        Bark.d("Assigning book (ID: $bookId) to shelf: $shelf")
        return shelfRemoteDataSource.assignShelf(
            ShelfAssignRequestDto(
                bookId = bookIdInt,
                shelf = shelf.toDto()
            )
        )
    }

    override suspend fun removeFromShelf(bookId: String): Result<Unit> {
        val bookIdInt = bookId.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid book ID: $bookId"))
        Bark.d("Removing book (ID: $bookId) from shelf")
        return shelfRemoteDataSource.removeShelf(bookIdInt)
    }
}
