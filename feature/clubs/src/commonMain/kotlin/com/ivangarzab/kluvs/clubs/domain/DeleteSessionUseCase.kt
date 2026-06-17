package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.Role

/**
 * UseCase for deleting an active reading session.
 *
 * Requires [Role.OWNER] authorization.
 */
class DeleteSessionUseCase(
    private val sessionRepository: SessionRepository
) : BaseAdminUseCase<DeleteSessionUseCase.Params, String>() {

    override val requiredRoles = OWNER_ONLY

    data class Params(val sessionId: String)

    override suspend fun execute(params: Params): Result<String> {
        Bark.d("Deleting session (Session ID: ${params.sessionId})")
        return sessionRepository.deleteSession(sessionId = params.sessionId)
            .onSuccess { Bark.i("Session deleted (Session ID: ${params.sessionId})") }
            .onFailure { Bark.e("Failed to delete session (Session ID: ${params.sessionId}). Retry.", it) }
    }
}
