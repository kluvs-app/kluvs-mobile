package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.DiscussionRepository
import com.ivangarzab.kluvs.model.Role

/**
 * UseCase for deleting a discussion from an active session.
 *
 * Requires [Role.ADMIN] or [Role.OWNER] authorization.
 */
class DeleteDiscussionUseCase(
    private val discussionRepository: DiscussionRepository
) : BaseAdminUseCase<DeleteDiscussionUseCase.Params, Unit>() {

    override val requiredRoles = ADMIN_AND_ABOVE

    data class Params(
        val discussionId: String
    )

    override suspend fun execute(params: Params): Result<Unit> {
        Bark.d("Deleting discussion (Discussion ID: ${params.discussionId})")
        return discussionRepository.deleteDiscussion(params.discussionId)
            .onSuccess { Bark.i("Discussion deleted (Discussion ID: ${params.discussionId})") }
            .onFailure { Bark.e("Failed to delete discussion (Discussion ID: ${params.discussionId}). Retry.", it) }
    }
}
