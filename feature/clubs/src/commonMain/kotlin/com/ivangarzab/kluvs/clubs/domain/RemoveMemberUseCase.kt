package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.UserFacingException

/**
 * UseCase for removing (kicking) a member from a club.
 *
 * Delegates to [MemberRepository.removeMemberFromClub], which uses the backend's
 * `remove_from_club` action. That action is authorized by the caller's role in the club
 * rather than by owning the target member's record, and deletes only this club's membership
 * (the target's other clubs are untouched). It no longer needs the target's current club
 * list, which could be stale.
 *
 * Restrictions:
 * - Requires [Role.OWNER] authorization (client-side gate; the backend also allows admins to
 *   remove regular members, which mobile does not expose yet).
 * - The acting user cannot remove themselves.
 */
class RemoveMemberUseCase(
    private val memberRepository: MemberRepository
) : BaseAdminUseCase<RemoveMemberUseCase.Params, Unit>() {

    override val requiredRoles = OWNER_ONLY

    data class Params(
        val memberId: String,
        val clubId: String,
        val currentMemberId: String
    )

    override suspend fun execute(params: Params): Result<Unit> {
        if (params.memberId == params.currentMemberId) {
            return Result.failure(UserFacingException("Cannot remove yourself from the club"))
        }

        Bark.d("Removing member from club (Member ID: ${params.memberId}, Club ID: ${params.clubId})")

        return memberRepository.removeMemberFromClub(params.memberId, params.clubId)
            .onSuccess { Bark.i("Member removed from club (Member ID: ${params.memberId}, Club ID: ${params.clubId})") }
            .onFailure { Bark.e("Failed to remove member from club (Member ID: ${params.memberId}). Retry.", it) }
    }
}
