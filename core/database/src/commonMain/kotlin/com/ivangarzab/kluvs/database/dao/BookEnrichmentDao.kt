package com.ivangarzab.kluvs.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ivangarzab.kluvs.database.entities.BookEnrichmentEntity

/**
 * Data Access Object for cached book enrichment responses.
 */
@Dao
interface BookEnrichmentDao {
    @Query("SELECT * FROM book_enrichment WHERE cacheKey = :cacheKey")
    suspend fun getEnrichment(cacheKey: String): BookEnrichmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrichment(enrichment: BookEnrichmentEntity)

    @Query("SELECT lastFetchedAt FROM book_enrichment WHERE cacheKey = :cacheKey")
    suspend fun getLastFetchedAt(cacheKey: String): Long?

    @Query("DELETE FROM book_enrichment")
    suspend fun deleteAll()
}
