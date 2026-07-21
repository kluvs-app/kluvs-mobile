package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.DiscussionRepository
import com.ivangarzab.kluvs.model.Discussion
import com.ivangarzab.kluvs.model.Role
import kotlinx.datetime.LocalDateTime

/**
 * UseCase for updating an existing discussion within an active session.
 *
 * Requires [Role.ADMIN] or [Role.OWNER] authorization.
 */
class UpdateDiscussionUseCase(
    private val discussionRepository: DiscussionRepository
) : BaseAdminUseCase<UpdateDiscussionUseCase.Params, Discussion>() {

    override val requiredRoles = ADMIN_AND_ABOVE

    data class Params(
        val discussionId: String,
        val title: String?,
        val location: String?,
        val date: LocalDateTime?
    )

    override suspend fun execute(params: Params): Result<Discussion> {
        Bark.d("Updating discussion (Discussion ID: ${params.discussionId})")
        return discussionRepository.updateDiscussion(
            discussionId = params.discussionId,
            title = params.title,
            date = params.date,
            location = params.location
        )
            .onSuccess { Bark.i("Discussion updated (Discussion ID: ${params.discussionId})") }
            .onFailure { Bark.e("Failed to update discussion (Discussion ID: ${params.discussionId}). Retry.", it) }
    }
}
