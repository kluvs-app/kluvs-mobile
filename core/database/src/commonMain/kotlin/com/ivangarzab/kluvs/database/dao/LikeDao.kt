package com.ivangarzab.kluvs.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ivangarzab.kluvs.database.entities.LikeEntity

/**
 * Data Access Object for Like entities.
 */
@Dao
interface LikeDao {
    @Query("SELECT * FROM likes WHERE bookId = :bookId")
    suspend fun getLike(bookId: String): LikeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity)

    @Delete
    suspend fun deleteLike(like: LikeEntity)

    @Query("SELECT lastFetchedAt FROM likes WHERE bookId = :bookId")
    suspend fun getLastFetchedAt(bookId: String): Long?

    @Query("DELETE FROM likes")
    suspend fun deleteAll()
}
