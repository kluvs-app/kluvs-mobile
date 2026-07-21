package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.DiscussionRepository
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Role
import kotlinx.datetime.LocalDateTime

/**
 * UseCase for creating a new discussion within an active session.
 *
 * Requires [Role.ADMIN] or [Role.OWNER] authorization.
 */
class CreateDiscussionUseCase(
    private val discussionRepository: DiscussionRepository
) : BaseAdminUseCase<CreateDiscussionUseCase.Params, Discussion>() {

    override val requiredRoles = ADMIN_AND_ABOVE

    data class Params(
        val sessionId: String,
        val title: String,
        val location: String,
        val date: LocalDateTime
    )

    override suspend fun execute(params: Params): Result<Discussion> {
        Bark.d("Creating discussion in session (Session ID: ${params.sessionId}, Title: ${params.title})")
        return discussionRepository.createDiscussion(
            sessionId = params.sessionId,
            title = params.title,
            date = params.date,
            location = params.location
        )
            .onSuccess { Bark.i("Discussion created in session (Session ID: ${params.sessionId})") }
            .onFailure { Bark.e("Failed to create discussion (Session ID: ${params.sessionId}). Retry.", it) }
    }
}
