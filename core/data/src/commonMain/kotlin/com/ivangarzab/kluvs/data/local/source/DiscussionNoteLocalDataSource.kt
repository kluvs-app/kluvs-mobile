package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.data.local.mappers.toEntity
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.model.DiscussionNote

/**
 * Local data source for the member's discussion notes.
 * Handles CRUD operations with the local Room database.
 */
interface DiscussionNoteLocalDataSource {
    suspend fun getNote(discussionId: String): DiscussionNote?
    suspend fun insertNote(note: DiscussionNote)
    suspend fun deleteNote(noteId: String)
    suspend fun getLastFetchedAt(discussionId: String): Long?
    suspend fun deleteAll()
}

/**
 * Implementation of [DiscussionNoteLocalDataSource] using Room database.
 */
class DiscussionNoteLocalDataSourceImpl(
    private val database: KluvsDatabase
) : DiscussionNoteLocalDataSource {

    private val discussionNoteDao = database.discussionNoteDao()

    override suspend fun getNote(discussionId: String): DiscussionNote? {
        return discussionNoteDao.getNote(discussionId)?.toDomain()
    }

    override suspend fun insertNote(note: DiscussionNote) {
        Bark.v("Inserting discussion note (ID: ${note.id}) into database")
        try {
            discussionNoteDao.insertNote(note.toEntity())
            Bark.d("Successfully inserted discussion note (ID: ${note.id}) into database")
        } catch (e: Exception) {
            Bark.e("Failed to insert discussion note (ID: ${note.id}) into database. Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun deleteNote(noteId: String) {
        val entity = discussionNoteDao.getNoteById(noteId)
        if (entity != null) {
            Bark.d("Deleting discussion note (ID: $noteId) from database")
            try {
                discussionNoteDao.deleteNote(entity)
                Bark.d("Successfully deleted discussion note (ID: $noteId) from database")
            } catch (e: Exception) {
                Bark.e("Failed to delete discussion note (ID: $noteId) from database. Retry on next sync.", e)
                throw e
            }
        }
    }

    override suspend fun getLastFetchedAt(discussionId: String): Long? {
        return discussionNoteDao.getLastFetchedAt(discussionId)
    }

    override suspend fun deleteAll() {
        Bark.d("Clearing all discussion notes from database")
        try {
            discussionNoteDao.deleteAll()
            Bark.d("Successfully cleared all discussion notes from database")
        } catch (e: Exception) {
            Bark.e("Failed to clear all discussion notes from database. Retry on next sync.", e)
            throw e
        }
    }
}
