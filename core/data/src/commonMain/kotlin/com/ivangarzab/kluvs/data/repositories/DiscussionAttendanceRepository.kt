package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceUpsertRequestDto
import com.ivangarzab.kluvs.data.local.source.DiscussionAttendanceLocalDataSource
import com.ivangarzab.kluvs.data.remote.mappers.toDto
import com.ivangarzab.kluvs.data.remote.source.DiscussionAttendanceRemoteDataSource
import com.ivangarzab.kluvs.model.AttendanceRoster
import com.ivangarzab.kluvs.model.AttendanceStatus

/**
 * Repository for discussion attendance (RSVPs).
 *
 * All operations are member-scoped: the backend resolves the member from the
 * caller's auth token, so a signed-in user session is required.
 */
interface DiscussionAttendanceRepository {

    /**
     * Retrieves the attendance roster for a discussion.
     *
     * @param discussionId The ID of the discussion
     * @return Result containing the [AttendanceRoster], including the caller's own RSVP
     */
    suspend fun getRoster(discussionId: String): Result<AttendanceRoster>

    /**
     * Sets or updates the member's RSVP for a discussion.
     *
     * @param discussionId The ID of the discussion to RSVP to
     * @param status The RSVP answer
     * @return Result containing the stored [AttendanceStatus] if successful
     */
    suspend fun setAttendance(discussionId: String, status: AttendanceStatus): Result<AttendanceStatus>

    /**
     * Clears the member's RSVP for a discussion (back to unanswered).
     *
     * @param discussionId The ID of the discussion
     * @return Result indicating success or failure
     */
    suspend fun clearAttendance(discussionId: String): Result<Unit>
}

/**
 * Implementation of [DiscussionAttendanceRepository] with a local mirror of the
 * caller's own RSVP.
 *
 * The full attendance roster (every member's response + name) is never cached —
 * [getRoster] always hits remote. Only the caller's own status is cached, since
 * the API hands that back directly as [AttendanceRoster.myStatus] without
 * requiring a locally-known member ID. That cached status is kept in sync via
 * write-through on [setAttendance]/[clearAttendance], and opportunistically
 * refreshed whenever [getRoster] succeeds.
 */
internal class DiscussionAttendanceRepositoryImpl(
    private val discussionAttendanceRemoteDataSource: DiscussionAttendanceRemoteDataSource,
    private val discussionAttendanceLocalDataSource: DiscussionAttendanceLocalDataSource
) : DiscussionAttendanceRepository {

    override suspend fun getRoster(discussionId: String): Result<AttendanceRoster> {
        val result = discussionAttendanceRemoteDataSource.getRoster(discussionId)

        result.onSuccess { roster ->
            Bark.v("Persisting my RSVP to cache (discussion ID: $discussionId)")
            try {
                val myStatus = roster.myStatus
                if (myStatus != null) {
                    discussionAttendanceLocalDataSource.setMyStatus(discussionId, myStatus)
                } else {
                    discussionAttendanceLocalDataSource.clearMyStatus(discussionId)
                }
            } catch (e: Exception) {
                Bark.e("RSVP cache failed. Will use remote data on next fetch.", e)
            }
        }.onFailure { error ->
            Bark.e("Failed to fetch attendance roster.", error)
        }

        return result
    }

    override suspend fun setAttendance(
        discussionId: String,
        status: AttendanceStatus
    ): Result<AttendanceStatus> {
        Bark.d("Setting RSVP for discussion (ID: $discussionId): $status")
        val result = discussionAttendanceRemoteDataSource.upsertAttendance(
            DiscussionAttendanceUpsertRequestDto(
                discussionId = discussionId,
                status = status.toDto()
            )
        )

        result.onSuccess { savedStatus ->
            Bark.v("Persisting RSVP to cache (discussion ID: $discussionId)")
            try {
                discussionAttendanceLocalDataSource.setMyStatus(discussionId, savedStatus)
                Bark.i("RSVP set and cached (discussion ID: $discussionId)")
            } catch (e: Exception) {
                Bark.e("RSVP cache failed. Will fetch from remote if needed.", e)
            }
        }.onFailure { error ->
            Bark.e("Setting RSVP failed. Check input and retry.", error)
        }

        return result
    }

    override suspend fun clearAttendance(discussionId: String): Result<Unit> {
        Bark.d("Clearing RSVP for discussion (ID: $discussionId)")
        val result = discussionAttendanceRemoteDataSource.clearAttendance(discussionId)

        result.onSuccess {
            Bark.v("Clearing cached RSVP (discussion ID: $discussionId)")
            discussionAttendanceLocalDataSource.clearMyStatus(discussionId)
        }.onFailure { error ->
            Bark.e("Clearing RSVP failed. Verify RSVP exists and retry.", error)
        }

        return result
    }
}
