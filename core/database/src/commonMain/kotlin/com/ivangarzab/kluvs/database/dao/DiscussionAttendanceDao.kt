package com.ivangarzab.kluvs.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ivangarzab.kluvs.database.entities.DiscussionAttendanceEntity

/**
 * Data Access Object for DiscussionAttendance entities.
 */
@Dao
interface DiscussionAttendanceDao {
    @Query("SELECT * FROM discussion_attendance WHERE discussionId = :discussionId")
    suspend fun getAttendance(discussionId: String): DiscussionAttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: DiscussionAttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: DiscussionAttendanceEntity)

    @Query("SELECT lastFetchedAt FROM discussion_attendance WHERE discussionId = :discussionId")
    suspend fun getLastFetchedAt(discussionId: String): Long?

    @Query("DELETE FROM discussion_attendance")
    suspend fun deleteAll()
}
