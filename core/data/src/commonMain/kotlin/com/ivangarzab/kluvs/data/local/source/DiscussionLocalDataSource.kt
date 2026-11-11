package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.database.KluvsDatabase

/**
 * Local data source for Discussion cache maintenance.
 *
 * Discussions are cached as part of their club (see [ClubLocalDataSource.insertClub]) and read
 * back through it; this source only keeps that cache honest after discussion mutations.
 */
interface DiscussionLocalDataSource {

    /** Removes a deleted discussion's cached row so it can't come back on a cached club read. */
    suspend fun deleteDiscussion(discussionId: String)

    /** Marks the club that owns [sessionId] stale, so its next read picks up a new discussion. */
    suspend fun markClubStaleForSession(sessionId: String)

    /** Marks the club that owns [discussionId] stale, so its next read picks up the change. */
    suspend fun markClubStaleForDiscussion(discussionId: String)
}

/**
 * Implementation of [DiscussionLocalDataSource] using Room database.
 */
class DiscussionLocalDataSourceImpl(
    private val database: KluvsDatabase
) : DiscussionLocalDataSource {

    private val discussionDao = database.discussionDao()
    private val sessionDao = database.sessionDao()
    private val clubDao = database.clubDao()

    override suspend fun deleteDiscussion(discussionId: String) {
        Bark.v("Removing discussion (ID: $discussionId) from database")
        try {
            discussionDao.deleteDiscussionById(discussionId)
        } catch (e: Exception) {
            Bark.e("Failed to remove discussion (ID: $discussionId) from database. Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun markClubStaleForSession(sessionId: String) {
        val clubId = sessionDao.getSession(sessionId)?.clubId
        if (clubId == null) {
            Bark.v("No cached club for session (ID: $sessionId); nothing to mark stale")
            return
        }
        Bark.v("Marking club (ID: $clubId) stale after a discussion change on session (ID: $sessionId)")
        clubDao.markStale(clubId)
    }

    override suspend fun markClubStaleForDiscussion(discussionId: String) {
        val sessionId = discussionDao.getDiscussion(discussionId)?.sessionId
        if (sessionId == null) {
            Bark.v("No cached session for discussion (ID: $discussionId); nothing to mark stale")
            return
        }
        markClubStaleForSession(sessionId)
    }
}
