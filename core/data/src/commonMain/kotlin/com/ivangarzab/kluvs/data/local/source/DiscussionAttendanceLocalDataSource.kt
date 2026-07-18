package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.data.local.mappers.toEntity
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.model.AttendanceStatus

/**
 * Local data source for the member's own discussion attendance (RSVP) status.
 *
 * Note: only caches the caller's own status, not the full attendance roster
 * (which includes other members' responses and names).
 */
interface DiscussionAttendanceLocalDataSource {
    suspend fun getMyStatus(discussionId: String): AttendanceStatus?
    suspend fun setMyStatus(discussionId: String, status: AttendanceStatus)
    suspend fun clearMyStatus(discussionId: String)
    suspend fun getLastFetchedAt(discussionId: String): Long?
    suspend fun deleteAll()
}

/**
 * Implementation of [DiscussionAttendanceLocalDataSource] using Room database.
 */
class DiscussionAttendanceLocalDataSourceImpl(
    private val database: KluvsDatabase
) : DiscussionAttendanceLocalDataSource {

    private val discussionAttendanceDao = database.discussionAttendanceDao()

    override suspend fun getMyStatus(discussionId: String): AttendanceStatus? {
        return discussionAttendanceDao.getAttendance(discussionId)?.toDomain()
    }

    override suspend fun setMyStatus(discussionId: String, status: AttendanceStatus) {
        Bark.v("Caching RSVP (discussion ID: $discussionId): $status")
        try {
            discussionAttendanceDao.insertAttendance(status.toEntity(discussionId))
            Bark.d("Successfully cached RSVP (discussion ID: $discussionId)")
        } catch (e: Exception) {
            Bark.e("Failed to cache RSVP (discussion ID: $discussionId). Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun clearMyStatus(discussionId: String) {
        val entity = discussionAttendanceDao.getAttendance(discussionId)
        if (entity != null) {
            Bark.d("Clearing cached RSVP (discussion ID: $discussionId)")
            try {
                discussionAttendanceDao.deleteAttendance(entity)
                Bark.d("Successfully cleared cached RSVP (discussion ID: $discussionId)")
            } catch (e: Exception) {
                Bark.e("Failed to clear cached RSVP (discussion ID: $discussionId). Retry on next sync.", e)
                throw e
            }
        }
    }

    override suspend fun getLastFetchedAt(discussionId: String): Long? {
        return discussionAttendanceDao.getLastFetchedAt(discussionId)
    }

    override suspend fun deleteAll() {
        Bark.d("Clearing all cached RSVPs from database")
        try {
            discussionAttendanceDao.deleteAll()
            Bark.d("Successfully cleared all cached RSVPs from database")
        } catch (e: Exception) {
            Bark.e("Failed to clear all cached RSVPs from database. Retry on next sync.", e)
            throw e
        }
    }
}
