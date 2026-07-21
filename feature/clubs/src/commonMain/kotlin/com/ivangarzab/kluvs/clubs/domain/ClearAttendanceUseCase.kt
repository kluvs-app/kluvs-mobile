package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.DiscussionAttendanceRepository

/**
 * UseCase for the signed-in member clearing their RSVP for a discussion
 * (tapping the already-selected attendance option).
 *
 * Self-serve — any club member may call this for themselves.
 *
 * @param discussionAttendanceRepository Repository for discussion attendance data
 */
class ClearAttendanceUseCase(
    private val discussionAttendanceRepository: DiscussionAttendanceRepository
) {
    suspend operator fun invoke(discussionId: String): Result<Unit> {
        Bark.d("Clearing RSVP (Discussion ID: $discussionId)")
        return discussionAttendanceRepository.clearAttendance(discussionId)
            .onSuccess { Bark.i("RSVP cleared (Discussion ID: $discussionId)") }
            .onFailure { Bark.e("Failed to clear RSVP (Discussion ID: $discussionId). Retry.", it) }
    }
}
