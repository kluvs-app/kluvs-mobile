package com.ivangarzab.kluvs.data.remote.source

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.api.models.DiscussionAttendanceUpsertRequestDto
import com.ivangarzab.kluvs.data.remote.api.DiscussionAttendanceService
import com.ivangarzab.kluvs.data.remote.mappers.toDomain
import com.ivangarzab.kluvs.model.AttendanceRoster
import com.ivangarzab.kluvs.model.AttendanceStatus

/**
 * Remote data source for discussion attendance (RSVPs).
 *
 * Responsibilities:
 * - Calls [com.ivangarzab.kluvs.data.remote.api.DiscussionAttendanceService] to fetch/mutate RSVP data from Supabase
 * - Maps DTOs to domain models using mappers
 * - Wraps results in [Result] for error handling
 */
interface DiscussionAttendanceRemoteDataSource {

    /**
     * Fetches the attendance roster for a discussion, including the caller's own RSVP.
     */
    suspend fun getRoster(discussionId: String): Result<AttendanceRoster>

    /**
     * Sets or updates the member's RSVP for a discussion, returning the stored status.
     */
    suspend fun upsertAttendance(request: DiscussionAttendanceUpsertRequestDto): Result<AttendanceStatus>

    /**
     * Clears the member's RSVP for a discussion (back to unanswered).
     */
    suspend fun clearAttendance(discussionId: String): Result<Unit>
}

class DiscussionAttendanceRemoteDataSourceImpl(
    private val discussionAttendanceService: DiscussionAttendanceService
) : DiscussionAttendanceRemoteDataSource {

    override suspend fun getRoster(discussionId: String): Result<AttendanceRoster> {
        return try {
            val roster = discussionAttendanceService.getRoster(discussionId).toDomain()
            Bark.d("Fetched attendance roster for discussion (ID: $discussionId, ${roster.responses.size} responses)")
            Result.success(roster)
        } catch (e: Exception) {
            Bark.e("Failed to fetch attendance roster for discussion (ID: $discussionId). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun upsertAttendance(
        request: DiscussionAttendanceUpsertRequestDto
    ): Result<AttendanceStatus> {
        return try {
            val status = discussionAttendanceService.upsert(request).status.toDomain()
            Bark.i("RSVP saved for discussion (ID: ${request.discussionId}, Status: $status)")
            Result.success(status)
        } catch (e: Exception) {
            Bark.e("Failed to save RSVP for discussion (ID: ${request.discussionId}). Please retry.", e)
            Result.failure(e)
        }
    }

    override suspend fun clearAttendance(discussionId: String): Result<Unit> {
        return try {
            discussionAttendanceService.clear(discussionId)
            Bark.i("RSVP cleared for discussion (ID: $discussionId)")
            Result.success(Unit)
        } catch (e: Exception) {
            Bark.e("Failed to clear RSVP for discussion (ID: $discussionId). Please retry.", e)
            Result.failure(e)
        }
    }
}
