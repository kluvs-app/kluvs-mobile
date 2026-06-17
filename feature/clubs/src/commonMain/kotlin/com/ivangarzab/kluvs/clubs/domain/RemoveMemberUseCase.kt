package com.ivangarzab.kluvs.clubs.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.MemberRepository
import com.ivangarzab.kluvs.model.Member
import com.ivangarzab.kluvs.model.Role

/**
 * UseCase for removing (kicking) a member from a club.
 *
 * Fetches the member's current clubs, removes the target club from the list,
 * and submits the updated memberships via [MemberRepository.updateMember].
 *
 * Restrictions:
 * - Requires [Role.OWNER] authorization.
 * - The acting user cannot remove themselves.
 */
class RemoveMemberUseCase(
    private val memberRepository: MemberRepository
) : BaseAdminUseCase<RemoveMemberUseCase.Params, Member>() {

    override val requiredRoles = OWNER_ONLY

    data class Params(
        val memberId: String,
        val clubId: String,
        val currentMemberId: String
    )

    override suspend fun execute(params: Params): Result<Member> {
        if (params.memberId == params.currentMemberId) {
            return Result.failure(IllegalArgumentException("Cannot remove yourself from the club"))
        }

        Bark.d("Removing member from club (Member ID: ${params.memberId}, Club ID: ${params.clubId})")

        val memberResult = memberRepository.getMember(params.memberId)
        if (memberResult.isFailure) {
            Bark.e("Failed to fetch member before removing from club (Member ID: ${params.memberId}).", memberResult.exceptionOrNull())
            return memberResult
        }

        val updatedClubIds = memberResult.getOrThrow().clubs
            ?.filter { it.id != params.clubId }
            ?.map { it.id }
            ?: emptyList()

        return memberRepository.updateMember(
            memberId = params.memberId,
            clubIds = updatedClubIds
        )
            .onSuccess { Bark.i("Member removed from club (Member ID: ${params.memberId}, Club ID: ${params.clubId})") }
            .onFailure { Bark.e("Failed to remove member from club (Member ID: ${params.memberId}). Retry.", it) }
    }
}
