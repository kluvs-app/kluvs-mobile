package com.ivangarzab.kluvs.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ivangarzab.kluvs.database.entities.DiscussionNoteEntity

/**
 * Data Access Object for DiscussionNote entities.
 */
@Dao
interface DiscussionNoteDao {
    @Query("SELECT * FROM discussion_notes WHERE discussionId = :discussionId")
    suspend fun getNote(discussionId: String): DiscussionNoteEntity?

    @Query("SELECT * FROM discussion_notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): DiscussionNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: DiscussionNoteEntity)

    @Delete
    suspend fun deleteNote(note: DiscussionNoteEntity)

    @Query("SELECT lastFetchedAt FROM discussion_notes WHERE discussionId = :discussionId")
    suspend fun getLastFetchedAt(discussionId: String): Long?

    @Query("DELETE FROM discussion_notes")
    suspend fun deleteAll()
}
