package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.SessionRepository
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.Session
import kotlinx.datetime.LocalDateTime

/**
 * UseCase for creating a new discussion within an active session.
 *
 * Fetches the current session, appends the new discussion, and submits the full updated list.
 * Requires [Role.ADMIN] or [Role.OWNER] authorization.
 */
class CreateDiscussionUseCase(
    private val sessionRepository: SessionRepository
) : BaseAdminUseCase<CreateDiscussionUseCase.Params, Session>() {

    override val requiredRoles = ADMIN_AND_ABOVE

    data class Params(
        val sessionId: String,
        val title: String,
        val location: String,
        val date: LocalDateTime
    )

    override suspend fun execute(params: Params): Result<Session> {
        Bark.d("Creating discussion in session (Session ID: ${params.sessionId}, Title: ${params.title})")

        val sessionResult = sessionRepository.getSession(params.sessionId)
        if (sessionResult.isFailure) {
            Bark.e("Failed to fetch session before creating discussion (Session ID: ${params.sessionId}).", sessionResult.exceptionOrNull())
            return sessionResult
        }

        val currentDiscussions = sessionResult.getOrThrow().discussions
        val newDiscussion = Discussion(
            id = "",
            sessionId = params.sessionId,
            title = params.title,
            location = params.location,
            date = params.date
        )
        val updatedDiscussions = currentDiscussions + newDiscussion

        return sessionRepository.updateSession(
            sessionId = params.sessionId,
            discussions = updatedDiscussions
        )
            .onSuccess { Bark.i("Discussion created in session (Session ID: ${params.sessionId})") }
            .onFailure { Bark.e("Failed to create discussion (Session ID: ${params.sessionId}). Retry.", it) }
    }
}
