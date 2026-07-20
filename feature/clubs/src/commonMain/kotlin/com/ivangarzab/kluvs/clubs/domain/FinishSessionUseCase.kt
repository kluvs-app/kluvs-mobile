package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.SessionRepository

/**
 * UseCase for ending the active reading session.
 *
 * The backend marks the session finished, credits `books_read` for all
 * participants with `is_reading = true`, and moves their book to the
 * "read" shelf. Requires [com.ivangarzab.kluvs.model.Role.ADMIN] or above.
 */
class FinishSessionUseCase(
    private val sessionRepository: SessionRepository
) : BaseAdminUseCase<FinishSessionUseCase.Params, Int?>() {

    override val requiredRoles = ADMIN_AND_ABOVE

    data class Params(
        val sessionId: String
    )

    /**
     * @return Result containing the number of members credited (null when the
     *         backend omits the count)
     */
    override suspend fun execute(params: Params): Result<Int?> {
        Bark.d("Finishing session (Session ID: ${params.sessionId})")
        return sessionRepository.finishSession(params.sessionId)
            .onSuccess { Bark.i("Session finished (Session ID: ${params.sessionId}, members credited: $it)") }
            .onFailure { Bark.e("Failed to finish session (Session ID: ${params.sessionId}). Retry.", it) }
    }
}
