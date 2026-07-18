package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.ShelfAssignRequestDto
import com.ivangarzab.kluvs.data.remote.api.ShelfService
import com.ivangarzab.kluvs.data.remote.mappers.toDomain
import com.ivangarzab.kluvs.model.ShelfEntry
import com.ivangarzab.kluvs.model.ShelfStatus

/**
 * Remote data source for the authenticated member's book shelf.
 *
 * Responsibilities:
 * - Calls [com.ivangarzab.kluvs.data.remote.api.ShelfService] to fetch/mutate shelf data from Supabase
 * - Maps DTOs to domain models using mappers
 * - Wraps results in [Result] for error handling
 */
interface ShelfRemoteDataSource {

    /**
     * Fetches the member's full shelf, with each entry's book populated.
     */
    suspend fun getShelf(): Result<List<ShelfEntry>>

    /**
     * Fetches the member's shelf status for a single book.
     *
     * Returns null inside the [Result] when the book is not shelved.
     */
    suspend fun getShelfStatus(bookId: Int): Result<ShelfStatus?>

    /**
     * Assigns a book to a shelf, returning the shelf it landed on.
     */
    suspend fun assignShelf(request: ShelfAssignRequestDto): Result<ShelfStatus>

    /**
     * Removes a book from the member's shelf.
     */
    suspend fun removeShelf(bookId: Int): Result<Unit>
}

class ShelfRemoteDataSourceImpl(
    private val shelfService: ShelfService
) : ShelfRemoteDataSource {

    override suspend fun getShelf(): Result<List<ShelfEntry>> {
        return try {
            val entries = shelfService.getAll().toDomain()
            Bark.d("Fetched shelf (${entries.size} entries)")
            Result.success(entries)
        } catch (e: Exception) {
            Bark.e("Failed to fetch shelf. Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun getShelfStatus(bookId: Int): Result<ShelfStatus?> {
        return try {
            val response = shelfService.getForBook(bookId)
            Bark.d("Fetched shelf status for book (ID: $bookId)")
            Result.success(response.shelf?.toDomain())
        } catch (e: Exception) {
            Bark.e("Failed to fetch shelf status for book (ID: $bookId). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun assignShelf(request: ShelfAssignRequestDto): Result<ShelfStatus> {
        return try {
            val response = shelfService.assign(request)
            val shelf = response.shelf
                ?: throw Exception("Shelf assignment succeeded but no shelf returned")
            Bark.i("Book shelved (ID: ${request.bookId}, Shelf: ${shelf.value})")
            Result.success(shelf.toDomain())
        } catch (e: Exception) {
            Bark.e("Failed to shelve book (ID: ${request.bookId}). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun removeShelf(bookId: Int): Result<Unit> {
        return try {
            val response = shelfService.remove(bookId)
            if (response.success == false) {
                throw Exception("Shelf removal failed for book (ID: $bookId)")
            }
            Bark.i("Book removed from shelf (ID: $bookId)")
            Result.success(Unit)
        } catch (e: Exception) {
            Bark.e("Failed to remove book from shelf (ID: $bookId). Please retry.", e)
            Result.failure(e)
        }
    }
}
