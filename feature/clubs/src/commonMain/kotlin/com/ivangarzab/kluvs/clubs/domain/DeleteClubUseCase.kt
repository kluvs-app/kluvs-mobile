package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.Role

/**
 * UseCase for deleting a club.
 *
 * Requires [Role.OWNER] authorization.
 */
class DeleteClubUseCase(
    private val clubRepository: ClubRepository
) : BaseAdminUseCase<DeleteClubUseCase.Params, String>() {

    override val requiredRoles = OWNER_ONLY

    data class Params(val clubId: String)

    override suspend fun execute(params: Params): Result<String> {
        Bark.d("Deleting club (Club ID: ${params.clubId})")
        return clubRepository.deleteClub(clubId = params.clubId)
            .onSuccess { Bark.i("Club deleted (Club ID: ${params.clubId})") }
            .onFailure { Bark.e("Failed to delete club (Club ID: ${params.clubId}). Retry.", it) }
    }
}
