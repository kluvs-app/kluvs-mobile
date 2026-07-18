package com.ivangarzab.kluvs.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ivangarzab.kluvs.database.entities.ProgressEntity

/**
 * Data Access Object for Progress entities.
 */
@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress WHERE id = :progressId")
    suspend fun getProgress(progressId: String): ProgressEntity?

    @Query(
        """
        SELECT * FROM progress
        WHERE (:bookId IS NULL OR bookId = :bookId)
        AND (:sessionId IS NULL OR sessionId = :sessionId)
        AND (:status IS NULL OR status = :status)
        """
    )
    suspend fun getProgressEntries(
        bookId: String?,
        sessionId: String?,
        status: String?
    ): List<ProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressEntries(entries: List<ProgressEntity>)

    @Delete
    suspend fun deleteProgress(progress: ProgressEntity)

    @Query("SELECT lastFetchedAt FROM progress WHERE id = :progressId")
    suspend fun getLastFetchedAt(progressId: String): Long?

    @Query("DELETE FROM progress")
    suspend fun deleteAll()
}
