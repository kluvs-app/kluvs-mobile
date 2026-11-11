package com.ivangarzab.kluvs.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ivangarzab.kluvs.database.entities.DiscussionEntity

/**
 * Data Access Object for Discussion entities.
 */
@Dao
interface DiscussionDao {
    @Query("SELECT * FROM discussions WHERE id = :discussionId")
    suspend fun getDiscussion(discussionId: String): DiscussionEntity?

    @Query("SELECT * FROM discussions WHERE sessionId = :sessionId")
    suspend fun getDiscussionsForSession(sessionId: String): List<DiscussionEntity>

    @Query("SELECT * FROM discussions")
    suspend fun getAllDiscussions(): List<DiscussionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscussion(discussion: DiscussionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscussions(discussions: List<DiscussionEntity>)

    @Delete
    suspend fun deleteDiscussion(discussion: DiscussionEntity)

    @Query("SELECT lastFetchedAt FROM discussions WHERE id = :discussionId")
    suspend fun getLastFetchedAt(discussionId: String): Long?

    @Query("DELETE FROM discussions")
    suspend fun deleteAll()

    @Query("DELETE FROM discussions WHERE sessionId = :sessionId")
    suspend fun deleteDiscussionsForSession(sessionId: String)

    @Query("DELETE FROM discussions WHERE id = :discussionId")
    suspend fun deleteDiscussionById(discussionId: String)
}
