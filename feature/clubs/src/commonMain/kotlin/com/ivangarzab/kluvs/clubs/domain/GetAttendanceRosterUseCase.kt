package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.DiscussionAttendanceRepository
import com.ivangarzab.kluvs.model.AttendanceRoster

/**
 * UseCase for fetching a discussion's attendance roster (RSVP responses + own status).
 *
 * Self-serve read — the backend resolves the caller from their auth token, so no
 * role gating is needed.
 *
 * @param discussionAttendanceRepository Repository for discussion attendance data
 */
class GetAttendanceRosterUseCase(
    private val discussionAttendanceRepository: DiscussionAttendanceRepository
) {
    suspend operator fun invoke(discussionId: String): Result<AttendanceRoster> {
        Bark.d("Fetching attendance roster (Discussion ID: $discussionId)")
        return discussionAttendanceRepository.getRoster(discussionId)
            .onSuccess { Bark.i("Loaded attendance roster (Discussion ID: $discussionId, Responses: ${it.responses.size})") }
            .onFailure { Bark.e("Failed to fetch attendance roster (Discussion ID: $discussionId).", it) }
    }
}
