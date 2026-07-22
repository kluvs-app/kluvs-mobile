package com.ivangarzab.kluvs.join.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.JoinRepository
import com.ivangarzab.kluvs.model.ClubPreview

/**
 * UseCase for previewing the club behind an invite token, without joining it.
 *
 * @param joinRepository Repository for the invite-link join flow
 */
class PreviewInviteUseCase(
    private val joinRepository: JoinRepository
) {
    suspend operator fun invoke(token: String): Result<ClubPreview> {
        Bark.d("Previewing invite via use case")
        return joinRepository.previewInvite(token)
    }
}
