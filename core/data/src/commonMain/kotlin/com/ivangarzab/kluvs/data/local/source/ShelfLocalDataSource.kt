package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.data.local.mappers.toEntity
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.model.ShelfEntry

/**
 * Local data source for Shelf entries.
 * Handles CRUD operations with the local Room database.
 */
interface ShelfLocalDataSource {
    suspend fun getShelf(): List<ShelfEntry>
    suspend fun getShelfEntry(bookId: String): ShelfEntry?
    suspend fun insertShelfEntry(entry: ShelfEntry)
    suspend fun insertShelfEntries(entries: List<ShelfEntry>)
    suspend fun deleteShelfEntry(bookId: String)
    suspend fun getLastFetchedAt(bookId: String): Long?
    suspend fun deleteAll()
}

/**
 * Implementation of [ShelfLocalDataSource] using Room database.
 */
class ShelfLocalDataSourceImpl(
    private val database: KluvsDatabase
) : ShelfLocalDataSource {

    private val shelfDao = database.shelfDao()
    private val bookDao = database.bookDao()

    override suspend fun getShelf(): List<ShelfEntry> {
        return shelfDao.getShelf().mapNotNull { entry ->
            val bookEntity = bookDao.getBook(entry.bookId) ?: return@mapNotNull null
            entry.toDomain(bookEntity.toDomain())
        }
    }

    override suspend fun getShelfEntry(bookId: String): ShelfEntry? {
        val entry = shelfDao.getShelfEntry(bookId) ?: return null
        val bookEntity = bookDao.getBook(bookId) ?: return null
        return entry.toDomain(bookEntity.toDomain())
    }

    override suspend fun insertShelfEntry(entry: ShelfEntry) {
        Bark.v("Inserting shelf entry (book ID: ${entry.book.id}) into database")
        try {
            bookDao.insertBook(entry.book.toEntity())
            shelfDao.insertShelfEntry(entry.toEntity())
            Bark.d("Successfully inserted shelf entry (book ID: ${entry.book.id}) into database")
        } catch (e: Exception) {
            Bark.e("Failed to insert shelf entry (book ID: ${entry.book.id}) into database. Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun insertShelfEntries(entries: List<ShelfEntry>) {
        Bark.v("Inserting ${entries.size} shelf entries into database")
        try {
            bookDao.insertBooks(entries.map { it.book.toEntity() })
            shelfDao.insertShelfEntries(entries.map { it.toEntity() })
            Bark.d("Successfully inserted ${entries.size} shelf entries into database")
        } catch (e: Exception) {
            Bark.e("Failed to insert ${entries.size} shelf entries into database. Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun deleteShelfEntry(bookId: String) {
        val entity = shelfDao.getShelfEntry(bookId)
        if (entity != null) {
            Bark.d("Deleting shelf entry (book ID: $bookId) from database")
            try {
                shelfDao.deleteShelfEntry(entity)
                Bark.d("Successfully deleted shelf entry (book ID: $bookId) from database")
            } catch (e: Exception) {
                Bark.e("Failed to delete shelf entry (book ID: $bookId) from database. Retry on next sync.", e)
                throw e
            }
        }
    }

    override suspend fun getLastFetchedAt(bookId: String): Long? {
        return shelfDao.getLastFetchedAt(bookId)
    }

    override suspend fun deleteAll() {
        Bark.d("Clearing all shelf entries from database")
        try {
            shelfDao.deleteAll()
            Bark.d("Successfully cleared all shelf entries from database")
        } catch (e: Exception) {
            Bark.e("Failed to clear all shelf entries from database. Retry on next sync.", e)
            throw e
        }
    }
}
