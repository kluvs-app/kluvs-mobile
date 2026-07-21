package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.DiscussionAttendanceRepository
import com.ivangarzab.kluvs.model.AttendanceStatus

/**
 * UseCase for the signed-in member setting or switching their RSVP for a discussion.
 *
 * Self-serve — any club member may call this for themselves, so unlike the
 * admin-gated session operations this does not extend [BaseAdminUseCase].
 *
 * @param discussionAttendanceRepository Repository for discussion attendance data
 */
class SetAttendanceUseCase(
    private val discussionAttendanceRepository: DiscussionAttendanceRepository
) {
    data class Params(
        val discussionId: String,
        val status: AttendanceStatus
    )

    suspend operator fun invoke(params: Params): Result<AttendanceStatus> {
        Bark.d("Setting RSVP (Discussion ID: ${params.discussionId}, Status: ${params.status})")
        return discussionAttendanceRepository.setAttendance(params.discussionId, params.status)
            .onSuccess { Bark.i("RSVP set (Discussion ID: ${params.discussionId}, Status: $it)") }
            .onFailure { Bark.e("Failed to set RSVP (Discussion ID: ${params.discussionId}). Retry.", it) }
    }
}
