package com.ivangarzab.kluvs.data.repositories

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceUpsertRequestDto
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

internal class DiscussionAttendanceRepositoryImpl(
    private val discussionAttendanceRemoteDataSource: DiscussionAttendanceRemoteDataSource
) : DiscussionAttendanceRepository {

    override suspend fun getRoster(discussionId: String): Result<AttendanceRoster> {
        return discussionAttendanceRemoteDataSource.getRoster(discussionId)
    }

    override suspend fun setAttendance(
        discussionId: String,
        status: AttendanceStatus
    ): Result<AttendanceStatus> {
        Bark.d("Setting RSVP for discussion (ID: $discussionId): $status")
        return discussionAttendanceRemoteDataSource.upsertAttendance(
            DiscussionAttendanceUpsertRequestDto(
                discussionId = discussionId,
                status = status.toDto()
            )
        )
    }

    override suspend fun clearAttendance(discussionId: String): Result<Unit> {
        Bark.d("Clearing RSVP for discussion (ID: $discussionId)")
        return discussionAttendanceRemoteDataSource.clearAttendance(discussionId)
    }
}
