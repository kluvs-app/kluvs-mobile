package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.Session
import kotlinx.datetime.LocalDateTime

/**
 * UseCase for updating an existing discussion within an active session.
 *
 * Fetches the current session, applies the changes to the target discussion,
 * and submits the full updated list. Requires [Role.ADMIN] or [Role.OWNER] authorization.
 */
class UpdateDiscussionUseCase(
    private val sessionRepository: SessionRepository
) : BaseAdminUseCase<UpdateDiscussionUseCase.Params, Session>() {

    override val requiredRoles = ADMIN_AND_ABOVE

    data class Params(
        val sessionId: String,
        val discussionId: String,
        val title: String?,
        val location: String?,
        val date: LocalDateTime?
    )

    override suspend fun execute(params: Params): Result<Session> {
        Bark.d("Updating discussion (Session ID: ${params.sessionId}, Discussion ID: ${params.discussionId})")

        val sessionResult = sessionRepository.getSession(params.sessionId)
        if (sessionResult.isFailure) {
            Bark.e("Failed to fetch session before updating discussion (Session ID: ${params.sessionId}).", sessionResult.exceptionOrNull())
            return sessionResult
        }

        val currentDiscussions = sessionResult.getOrThrow().discussions
        val updatedDiscussions = currentDiscussions.map { discussion ->
            if (discussion.id == params.discussionId) {
                discussion.copy(
                    title = params.title ?: discussion.title,
                    location = params.location ?: discussion.location,
                    date = params.date ?: discussion.date
                )
            } else {
                discussion
            }
        }

        return sessionRepository.updateSession(
            sessionId = params.sessionId,
            discussions = updatedDiscussions
        )
            .onSuccess { Bark.i("Discussion updated (Discussion ID: ${params.discussionId})") }
            .onFailure { Bark.e("Failed to update discussion (Discussion ID: ${params.discussionId}). Retry.", it) }
    }
}
