package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.local.mappers.toEntity
import com.ivangarzab.kluvs.database.KluvsDatabase

/**
 * Local data source for the member's book like state.
 * Handles CRUD operations with the local Room database.
 */
interface LikeLocalDataSource {
    suspend fun getLikeStatus(bookId: String): Boolean?
    suspend fun setLikeStatus(bookId: String, liked: Boolean)
    suspend fun getLastFetchedAt(bookId: String): Long?
    suspend fun deleteAll()
}

/**
 * Implementation of [LikeLocalDataSource] using Room database.
 */
class LikeLocalDataSourceImpl(
    private val database: KluvsDatabase
) : LikeLocalDataSource {

    private val likeDao = database.likeDao()

    override suspend fun getLikeStatus(bookId: String): Boolean? {
        return likeDao.getLike(bookId)?.liked
    }

    override suspend fun setLikeStatus(bookId: String, liked: Boolean) {
        Bark.v("Caching like status (book ID: $bookId): $liked")
        try {
            likeDao.insertLike(liked.toEntity(bookId))
            Bark.d("Successfully cached like status (book ID: $bookId)")
        } catch (e: Exception) {
            Bark.e("Failed to cache like status (book ID: $bookId). Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun getLastFetchedAt(bookId: String): Long? {
        return likeDao.getLastFetchedAt(bookId)
    }

    override suspend fun deleteAll() {
        Bark.d("Clearing all like statuses from database")
        try {
            likeDao.deleteAll()
            Bark.d("Successfully cleared all like statuses from database")
        } catch (e: Exception) {
            Bark.e("Failed to clear all like statuses from database. Retry on next sync.", e)
            throw e
        }
    }
}
