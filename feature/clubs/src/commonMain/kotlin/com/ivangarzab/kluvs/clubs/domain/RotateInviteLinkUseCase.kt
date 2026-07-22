package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.ClubRepository
import com.ivangarzab.kluvs.model.Club
import com.ivangarzab.kluvs.model.JoinPolicy

/**
 * UseCase for rotating a club's active invite token.
 *
 * There is no atomic "rotate while staying active" API call — this is implemented as a
 * two-call round trip (deactivate via [JoinPolicy.PRIVATE], then reactivate via
 * [JoinPolicy.INVITE_LINK], which creates a fresh token). This is not atomic: a concurrent
 * invite preview could see the club as private for the brief gap between calls. If the
 * second call fails after the first succeeds, the club is left PRIVATE — that failure is
 * surfaced as [RotateInviteLinkException] so the caller can distinguish it from an ordinary
 * failure and prompt the user to retry rather than implying nothing happened.
 *
 * Requires [com.ivangarzab.kluvs.model.Role.OWNER] authorization.
 */
class RotateInviteLinkUseCase(
    private val clubRepository: ClubRepository
) : BaseAdminUseCase<RotateInviteLinkUseCase.Params, Club>() {

    override val requiredRoles = OWNER_ONLY

    data class Params(val clubId: String)

    override suspend fun execute(params: Params): Result<Club> {
        Bark.d("Rotating invite link (Club ID: ${params.clubId})")

        val deactivateResult = clubRepository.updateClub(clubId = params.clubId, joinPolicy = JoinPolicy.PRIVATE)
        if (deactivateResult.isFailure) {
            Bark.e(
                "Failed to deactivate invite link before rotation (Club ID: ${params.clubId}). Retry.",
                deactivateResult.exceptionOrNull()
            )
            return deactivateResult
        }

        return clubRepository.updateClub(clubId = params.clubId, joinPolicy = JoinPolicy.INVITE_LINK)
            .onSuccess { Bark.i("Rotated invite link (Club ID: ${params.clubId})") }
            .onFailure { error ->
                Bark.e(
                    "Invite link deactivated but rotation failed to reactivate (Club ID: ${params.clubId}). " +
                        "Club is now PRIVATE.",
                    error
                )
            }
            .recoverCatching { error -> throw RotateInviteLinkException(error) }
    }
}

/**
 * Thrown when [RotateInviteLinkUseCase]'s second call fails after the first succeeded,
 * leaving the club deactivated (PRIVATE) rather than in its original INVITE_LINK state.
 */
class RotateInviteLinkException(cause: Throwable) :
    Exception("Invite link deactivated but rotation failed — try again", cause)
