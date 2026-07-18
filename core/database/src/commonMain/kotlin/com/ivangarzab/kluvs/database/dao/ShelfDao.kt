package com.ivangarzab.kluvs.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ivangarzab.kluvs.database.entities.ShelfEntity

/**
 * Data Access Object for Shelf entities.
 */
@Dao
interface ShelfDao {
    @Query("SELECT * FROM shelf WHERE bookId = :bookId")
    suspend fun getShelfEntry(bookId: String): ShelfEntity?

    @Query("SELECT * FROM shelf")
    suspend fun getShelf(): List<ShelfEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelfEntry(entry: ShelfEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelfEntries(entries: List<ShelfEntity>)

    @Delete
    suspend fun deleteShelfEntry(entry: ShelfEntity)

    @Query("SELECT lastFetchedAt FROM shelf WHERE bookId = :bookId")
    suspend fun getLastFetchedAt(bookId: String): Long?

    @Query("DELETE FROM shelf")
    suspend fun deleteAll()
}
