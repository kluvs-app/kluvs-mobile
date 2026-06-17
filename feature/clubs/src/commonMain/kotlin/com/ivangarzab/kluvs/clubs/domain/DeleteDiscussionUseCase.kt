package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.Session

/**
 * UseCase for deleting a discussion from an active session.
 *
 * Requires [Role.ADMIN] or [Role.OWNER] authorization.
 */
class DeleteDiscussionUseCase(
    private val sessionRepository: SessionRepository
) : BaseAdminUseCase<DeleteDiscussionUseCase.Params, Session>() {

    override val requiredRoles = ADMIN_AND_ABOVE

    data class Params(
        val sessionId: String,
        val discussionId: String
    )

    override suspend fun execute(params: Params): Result<Session> {
        Bark.d("Deleting discussion (Session ID: ${params.sessionId}, Discussion ID: ${params.discussionId})")
        return sessionRepository.updateSession(
            sessionId = params.sessionId,
            discussionIdsToDelete = listOf(params.discussionId)
        )
            .onSuccess { Bark.i("Discussion deleted (Discussion ID: ${params.discussionId})") }
            .onFailure { Bark.e("Failed to delete discussion (Discussion ID: ${params.discussionId}). Retry.", it) }
    }
}
