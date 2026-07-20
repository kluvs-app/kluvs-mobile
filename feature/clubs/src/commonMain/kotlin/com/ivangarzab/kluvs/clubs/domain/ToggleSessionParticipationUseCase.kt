package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.SessionRepository

/**
 * UseCase for the signed-in member opting in/out of the active reading session
 * ("Join this Read" / "Opt out" on the Overview tab).
 *
 * Self-serve — any club member may call this for themselves, so unlike the
 * admin-gated session operations this does not extend [BaseAdminUseCase].
 *
 * @param sessionRepository Repository for session data
 */
class ToggleSessionParticipationUseCase(
    private val sessionRepository: SessionRepository
) {
    data class Params(
        val sessionId: String,
        val memberId: String,
        val isReading: Boolean
    )

    suspend operator fun invoke(params: Params): Result<Unit> {
        Bark.d("Toggling session participation (Session ID: ${params.sessionId}, Member ID: ${params.memberId}, isReading: ${params.isReading})")
        return sessionRepository.updateParticipation(params.sessionId, params.memberId, params.isReading)
            .onSuccess { Bark.i("Session participation updated (Member ID: ${params.memberId}, isReading: ${params.isReading})") }
            .onFailure { Bark.e("Failed to update session participation (Member ID: ${params.memberId}). Retry.", it) }
    }
}
