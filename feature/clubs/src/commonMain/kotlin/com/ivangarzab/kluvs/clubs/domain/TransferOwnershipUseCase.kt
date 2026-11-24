package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Role
import com.ivangarzab.kluvs.model.UserFacingException

/**
 * UseCase for transferring club ownership from the signed-in owner to another member.
 *
 * Promotes [Params.newOwnerId] to [Role.OWNER], then demotes [Params.currentOwnerId] to
 * [Role.ADMIN]. Backend authorizes this via `club_roles` on the `member` PUT endpoint — the
 * caller must currently be the club's owner (see kluvs-backend's `handleUpdateMember`).
 *
 * The new owner is promoted first so there is never a zero-owner window. If the second call
 * (demoting the previous owner) fails, the club is left with two owners until a retry or a
 * future refresh surfaces the real state.
 *
 * Restrictions:
 * - Requires [Role.OWNER] authorization.
 */
class TransferOwnershipUseCase(
    private val memberRepository: MemberRepository
) : BaseAdminUseCase<TransferOwnershipUseCase.Params, Member>() {

    override val requiredRoles = OWNER_ONLY

    data class Params(
        val currentOwnerId: String,
        val newOwnerId: String,
        val clubId: String
    )

    override suspend fun execute(params: Params): Result<Member> {
        if (params.newOwnerId == params.currentOwnerId) {
            return Result.failure(UserFacingException("Already the owner"))
        }

        Bark.d("Transferring club ownership (Club ID: ${params.clubId}, New Owner: ${params.newOwnerId})")

        val promoteResult = memberRepository.updateMember(
            memberId = params.newOwnerId,
            clubRoles = mapOf(params.clubId to Role.OWNER.name.lowercase())
        )
        if (promoteResult.isFailure) {
            Bark.e("Failed to promote new owner (Club ID: ${params.clubId}, New Owner: ${params.newOwnerId}). Retry.", promoteResult.exceptionOrNull())
            return promoteResult
        }

        return memberRepository.updateMember(
            memberId = params.currentOwnerId,
            clubRoles = mapOf(params.clubId to Role.ADMIN.name.lowercase())
        )
            .onSuccess { Bark.i("Ownership transferred (Club ID: ${params.clubId}, New Owner: ${params.newOwnerId})") }
            .onFailure { Bark.e("New owner promoted but failed to demote previous owner (Club ID: ${params.clubId}). Manual retry needed.", it) }
    }
}
