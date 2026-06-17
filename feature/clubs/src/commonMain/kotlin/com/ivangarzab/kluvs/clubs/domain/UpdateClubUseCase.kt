package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.Role

/**
 * UseCase for updating a club's name.
 *
 * Requires [Role.OWNER] authorization.
 */
class UpdateClubUseCase(
    private val clubRepository: ClubRepository
) : BaseAdminUseCase<UpdateClubUseCase.Params, Club>() {

    override val requiredRoles = OWNER_ONLY

    data class Params(val clubId: String, val newName: String)

    override suspend fun execute(params: Params): Result<Club> {
        Bark.d("Updating club name (Club ID: ${params.clubId})")
        return clubRepository.updateClub(clubId = params.clubId, name = params.newName)
            .onSuccess { Bark.i("Club name updated (Club ID: ${params.clubId})") }
            .onFailure { Bark.e("Failed to update club name (Club ID: ${params.clubId}). Retry.", it) }
    }
}
