package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.JoinPolicy

/**
 * UseCase for toggling a club's join policy (invite link on/off).
 *
 * Requires [com.ivangarzab.kluvs.model.Role.OWNER] authorization.
 */
class UpdateJoinPolicyUseCase(
    private val clubRepository: ClubRepository
) : BaseAdminUseCase<UpdateJoinPolicyUseCase.Params, Club>() {

    override val requiredRoles = OWNER_ONLY

    data class Params(val clubId: String, val joinPolicy: JoinPolicy)

    override suspend fun execute(params: Params): Result<Club> {
        Bark.d("Updating join policy (Club ID: ${params.clubId}, policy: ${params.joinPolicy})")
        return clubRepository.updateClub(clubId = params.clubId, joinPolicy = params.joinPolicy)
            .onSuccess { Bark.i("Join policy updated (Club ID: ${params.clubId})") }
            .onFailure { Bark.e("Failed to update join policy (Club ID: ${params.clubId}). Retry.", it) }
    }
}
