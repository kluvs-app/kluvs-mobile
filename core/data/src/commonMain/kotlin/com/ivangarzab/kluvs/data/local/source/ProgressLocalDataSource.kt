package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.data.local.mappers.toEntity
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.model.BookSummary
import com.ivangarzab.kluvs.model.ReadingProgress

/**
 * Local data source for the member's reading progress entries.
 * Handles CRUD operations with the local Room database.
 */
interface ProgressLocalDataSource {
    suspend fun getProgress(
        bookId: String? = null,
        sessionId: String? = null,
        status: String? = null
    ): List<ReadingProgress>

    suspend fun insertProgress(progress: ReadingProgress)
    suspend fun deleteProgress(progressId: String)
    suspend fun getLastFetchedAt(progressId: String): Long?
    suspend fun deleteAll()
}

/**
 * Implementation of [ProgressLocalDataSource] using Room database.
 */
class ProgressLocalDataSourceImpl(
    private val database: KluvsDatabase
) : ProgressLocalDataSource {

    private val progressDao = database.progressDao()
    private val bookDao = database.bookDao()

    override suspend fun getProgress(
        bookId: String?,
        sessionId: String?,
        status: String?
    ): List<ReadingProgress> {
        return progressDao.getProgressEntries(bookId, sessionId, status).map { entity ->
            val bookSummary = bookDao.getBook(entity.bookId)?.let {
                BookSummary(
                    id = it.id,
                    title = it.title,
                    author = it.author,
                    pageCount = it.pageCount,
                    imageUrl = it.imageUrl
                )
            }
            entity.toDomain(bookSummary)
        }
    }

    override suspend fun insertProgress(progress: ReadingProgress) {
        Bark.v("Inserting progress entry (ID: ${progress.id}) into database")
        try {
            progressDao.insertProgress(progress.toEntity())
            Bark.d("Successfully inserted progress entry (ID: ${progress.id}) into database")
        } catch (e: Exception) {
            Bark.e("Failed to insert progress entry (ID: ${progress.id}) into database. Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun deleteProgress(progressId: String) {
        val entity = progressDao.getProgress(progressId)
        if (entity != null) {
            Bark.d("Deleting progress entry (ID: $progressId) from database")
            try {
                progressDao.deleteProgress(entity)
                Bark.d("Successfully deleted progress entry (ID: $progressId) from database")
            } catch (e: Exception) {
                Bark.e("Failed to delete progress entry (ID: $progressId) from database. Retry on next sync.", e)
                throw e
            }
        }
    }

    override suspend fun getLastFetchedAt(progressId: String): Long? {
        return progressDao.getLastFetchedAt(progressId)
    }

    override suspend fun deleteAll() {
        Bark.d("Clearing all progress entries from database")
        try {
            progressDao.deleteAll()
            Bark.d("Successfully cleared all progress entries from database")
        } catch (e: Exception) {
            Bark.e("Failed to clear all progress entries from database. Retry on next sync.", e)
            throw e
        }
    }
}
